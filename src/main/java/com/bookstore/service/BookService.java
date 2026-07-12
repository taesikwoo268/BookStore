package com.bookstore.service;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookFilterRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.response.PageResponse;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateISBNException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.validation.BookValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    public Book createBook(BookCreateRequest request) {
        Book book = bookMapper.toEntity(request);
        BookValidator.validate(book);
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateISBNException("Book with ISBN " + book.getIsbn() + " already exists");
        }
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookUpdateRequest request) {
        Book existing = getBookById(id);

        // Check duplicate ISBN (excluding itself)
        if (!existing.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateISBNException("ISBN " + request.getIsbn() + " already used by another book");
        }

        // Map update request to existing entity
        bookMapper.updateEntity(existing, request);
        BookValidator.validate(existing);

        return bookRepository.save(existing);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    public List<Book> searchBook(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return bookRepository.findAll();
        }
        String lowerKeyword = keyword.toLowerCase();
        return bookRepository.findAll().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerKeyword) ||
                        b.getIsbn().contains(keyword) ||
                        (b.getAuthor() != null && b.getAuthor().getName().toLowerCase().contains(lowerKeyword)) ||
                        (b.getCategories() != null && b.getCategories().stream()
                                .anyMatch(c -> c.getName().toLowerCase().contains(lowerKeyword))))
                .collect(Collectors.toList());
    }

    // Stream API: lọc sách theo giá (từ -> đến)
    public List<Book> filterByPrice(BigDecimal from, BigDecimal to) {
        return bookRepository.findAll().stream()
                .filter(b -> (from == null || b.getPrice().compareTo(from) >= 0) &&
                        (to == null || b.getPrice().compareTo(to) <= 0))
                .collect(Collectors.toList());
    }

    // Stream API: nhóm theo category
    public Map<String, List<Book>> groupByCategory() {

        return bookRepository.findAll().stream()
                .filter(book -> book.getCategories() != null && !book.getCategories().isEmpty())
                .flatMap(book -> book.getCategories().stream()
                        .map(category -> Map.entry(category.getName(), book)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    // Stream API: top 5 sách bán chạy nhất (dựa trên salesCount)
    public List<Book> top5BestSellers() {
        return bookRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getSalesCount().compareTo(b1.getSalesCount()))
                .limit(5)
                .collect(Collectors.toList());
    }
    /**
     * Get books with pagination, sorting and filtering
     */
    public PageResponse<Book> getBooksWithFilter(BookFilterRequest filter) {

        // 1. Lấy tất cả books
        List<Book> allBooks = bookRepository.findAll();

        // 2. Apply filters
        List<Book> filteredBooks = applyFilters(allBooks, filter);

        // 3. Apply sorting
        List<Book> sortedBooks = applySorting(filteredBooks, filter.getSort());

        // 4. Apply pagination
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;

        return createPageResponse(sortedBooks, page, size);
    }

    /**
     * Apply all filters
     */
    private List<Book> applyFilters(List<Book> books, BookFilterRequest filter) {
        return books.stream()
                .filter(book -> filterByGenre(book, filter.getGenre()))
                .filter(book -> filterByPrice(book, filter.getMinPrice(), filter.getMaxPrice()))
                .collect(Collectors.toList());
    }

    /**
     * Filter by genre (category name)
     */
    private boolean filterByGenre(Book book, String genre) {
        if (genre == null || genre.isBlank()) {
            return true;
        }
        if (book.getCategories() == null|| book.getCategories().isEmpty()) {
            return false;
        }
        return book.getCategories().stream().anyMatch(category -> category.getName().equalsIgnoreCase(genre));
    }

    /**
     * Filter by price range
     */
    private boolean filterByPrice(Book book, BigDecimal minPrice, BigDecimal maxPrice) {
        if (book.getPrice() == null) {
            return false;
        }

        boolean minCondition = minPrice == null || book.getPrice().compareTo(minPrice) >= 0;
        boolean maxCondition = maxPrice == null || book.getPrice().compareTo(maxPrice) <= 0;

        return minCondition && maxCondition;
    }

    /**
     * Apply sorting
     * Format: "field,order" (e.g., "price,asc" or "title,desc")
     */
    private List<Book> applySorting(List<Book> books, String sort) {
        if (sort == null || sort.isBlank()) {
            return books; // Default: no sorting
        }

        String[] sortParts = sort.split(",");
        String field = sortParts[0];
        String order = sortParts.length > 1 ? sortParts[1] : "asc";

        return books.stream()
                .sorted((b1, b2) -> {
                    int result = compareByField(b1, b2, field);
                    return "desc".equalsIgnoreCase(order) ? -result : result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Compare two books by field
     */
    private int compareByField(Book b1, Book b2, String field) {
        switch (field.toLowerCase()) {
            case "id":
                return b1.getId().compareTo(b2.getId());
            case "isbn":
                return b1.getIsbn().compareTo(b2.getIsbn());
            case "title":
                return b1.getTitle().compareTo(b2.getTitle());
            case "price":
                return b1.getPrice().compareTo(b2.getPrice());
            case "stock":
                return b1.getStock().compareTo(b2.getStock());
            case "salescount":
                return b1.getSalesCount().compareTo(b2.getSalesCount());
            default:
                return b1.getId().compareTo(b2.getId());
        }
    }

    /**
     * Create PageResponse from list
     */
    private PageResponse<Book> createPageResponse(List<Book> books, int page, int size) {
        int total = books.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<Book> pageContent = fromIndex < total ? books.subList(fromIndex, toIndex) : List.of();

        return PageResponse.<Book>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / size))
                .first(page == 0)
                .last(fromIndex + size >= total)
                .empty(pageContent.isEmpty())
                .numberOfElements(pageContent.size())
                .build();
    }

    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class, OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public Book updateBookWithVersion(Long id, BookUpdateRequest request) {
        // Lấy book hiện tại (version sẽ được load)
        Book existing = getBookById(id);

        // Check duplicate ISBN (excluding itself)
        if (!existing.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateISBNException("ISBN " + request.getIsbn() + " already used by another book");
        }

        // Map update request to existing entity
        bookMapper.updateEntity(existing, request);
        BookValidator.validate(existing);
        // Lưu - nếu version thay đổi (bởi người khác), sẽ throw OptimisticLockException
        return bookRepository.save(existing);
    }
}