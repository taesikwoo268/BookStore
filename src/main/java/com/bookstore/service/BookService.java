package com.bookstore.service;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookFilterRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.response.BookDetailResponse;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.dto.response.PageResponse;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateISBNException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.validation.BookValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    // ============================================================
    // 1. GET ALL BOOKS - CACHE DTO
    // ============================================================

    @Cacheable(value = "bookList", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> getAllBooks() {
        log.info("📚 [CACHE MISS] getAllBooks - Loading all books from database");
        List<Book> books = bookRepository.findAll();
        return bookMapper.toSummaryList(books);
    }

    // ============================================================
    // 2. GET BOOK BY ID - CACHE DTO
    // ============================================================

    @Cacheable(value = "bookDetail", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public BookDetailResponse getBookById(Long id) {
        log.info("📚 [CACHE MISS] getBookById - Loading book: id={}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return bookMapper.toDetailResponse(book);
    }

    // ============================================================
    // 3. GET BOOK BY ISBN - CACHE DTO
    // ============================================================

    @Cacheable(value = "bookDetail", key = "#isbn", unless = "#result == null")
    @Transactional(readOnly = true)
    public BookDetailResponse getBookByIsbn(String isbn) {
        log.info("📚 [CACHE MISS] getBookByIsbn - Loading book: isbn={}", isbn);
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book not found with isbn: " + isbn));
        return bookMapper.toDetailResponse(book);
    }

    // ============================================================
    // 4. GET BOOK ENTITY (Internal use, không cache)
    // ============================================================

    @Transactional(readOnly = true)
    public Book getBookEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Book getBookEntityByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book not found with isbn: " + isbn));
    }

    // ============================================================
    // 5. CREATE BOOK - EVICT CACHE
    // ============================================================

    @CacheEvict(value = {"bookList", "homepageFeatured", "topSellers"}, allEntries = true)
    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        log.info("📝 [CACHE EVICT] createBook - Creating new book: isbn={}", request.getIsbn());

        Book book = bookMapper.toEntity(request);
        BookValidator.validate(book);

        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateISBNException("Book with ISBN " + book.getIsbn() + " already exists");
        }

        Book savedBook = bookRepository.save(book);
        log.info("✅ Book created successfully: id={}", savedBook.getId());
        return bookMapper.toResponse(savedBook);
    }

    // ============================================================
    // 6. UPDATE BOOK - UPDATE CACHE + EVICT
    // ============================================================

    @CachePut(value = "bookDetail", key = "#result.isbn", condition = "#result != null")
    @CacheEvict(value = {"bookList", "homepageFeatured", "topSellers"}, allEntries = true)
    @Transactional
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        log.info("📝 [CACHE UPDATE] updateBook - Updating book: id={}", id);

        Book existing = getBookEntityById(id);

        // Check duplicate ISBN (excluding itself)
        if (!existing.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateISBNException("ISBN " + request.getIsbn() + " already used by another book");
        }

        bookMapper.updateEntity(existing, request);
        BookValidator.validate(existing);

        Book updatedBook = bookRepository.save(existing);
        log.info("✅ Book updated successfully: id={}", updatedBook.getId());
        return bookMapper.toResponse(updatedBook);
    }

    // ============================================================
    // 7. DELETE BOOK - EVICT CACHE
    // ============================================================

    @CacheEvict(value = {"bookDetail", "bookList", "homepageFeatured", "topSellers"}, allEntries = true)
    @Transactional
    public void deleteBook(Long id) {
        log.info("🗑️ [CACHE EVICT] deleteBook - Deleting book: id={}", id);

        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with id: " + id);
        }

        bookRepository.deleteById(id);
        log.info("✅ Book deleted successfully: id={}", id);
    }

    @CacheEvict(value = {"bookDetail", "bookList", "homepageFeatured", "topSellers"}, allEntries = true)
    @Transactional
    public void deleteBookByIsbn(String isbn) {
        log.info("🗑️ [CACHE EVICT] deleteBookByIsbn - Deleting book: isbn={}", isbn);

        Book book = getBookEntityByIsbn(isbn);
        bookRepository.delete(book);
        log.info("✅ Book deleted successfully: isbn={}", isbn);
    }

    // ============================================================
    // 8. SEARCH BOOK - KHÔNG CACHE (dữ liệu động)
    // ============================================================

    @Transactional(readOnly = true)
    public List<BookSummaryResponse> searchBook(String keyword) {
        log.info("🔍 searchBook - Searching books: keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return getAllBooks();
        }

        String lowerKeyword = keyword.toLowerCase();
        List<Book> books = bookRepository.findAll().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerKeyword) ||
                        b.getIsbn().contains(keyword) ||
                        (b.getAuthor() != null && b.getAuthor().getName().toLowerCase().contains(lowerKeyword)) ||
                        (b.getCategories() != null && b.getCategories().stream()
                                .anyMatch(c -> c.getName().toLowerCase().contains(lowerKeyword))))
                .collect(Collectors.toList());

        return bookMapper.toSummaryList(books);
    }

    // ============================================================
    // 9. FILTER BY PRICE - KHÔNG CACHE
    // ============================================================

    @Transactional(readOnly = true)
    public List<BookSummaryResponse> filterByPrice(BigDecimal from, BigDecimal to) {
        log.info("💰 filterByPrice - Filtering books: from={}, to={}", from, to);

        List<Book> books = bookRepository.findAll().stream()
                .filter(b -> (from == null || b.getPrice().compareTo(from) >= 0) &&
                        (to == null || b.getPrice().compareTo(to) <= 0))
                .collect(Collectors.toList());

        return bookMapper.toSummaryList(books);
    }

    // ============================================================
    // 10. GROUP BY CATEGORY - KHÔNG CACHE
    // ============================================================

    @Transactional(readOnly = true)
    public Map<String, List<BookSummaryResponse>> groupByCategory() {
        log.info("📂 groupByCategory - Grouping books by category");

        Map<String, List<Book>> grouped = bookRepository.findAll().stream()
                .filter(book -> book.getCategories() != null && !book.getCategories().isEmpty())
                .flatMap(book -> book.getCategories().stream()
                        .map(category -> Map.entry(category.getName(), book)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> bookMapper.toSummaryList(e.getValue())
                ));
    }

    // ============================================================
    // 11. TOP 5 BEST SELLERS - CACHE DTO
    // ============================================================

    @Cacheable(value = "topSellers", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> top5BestSellers() {
        log.info("🏆 [CACHE MISS] top5BestSellers - Loading from database");

        List<Book> books = bookRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getSalesCount().compareTo(b1.getSalesCount()))
                .limit(5)
                .collect(Collectors.toList());

        return bookMapper.toSummaryList(books);
    }

    // ============================================================
    // 12. GET BOOKS WITH FILTER (Pagination) - KHÔNG CACHE
    // ============================================================

    @Transactional(readOnly = true)
    public PageResponse<BookSummaryResponse> getBooksWithFilter(BookFilterRequest filter) {
        log.info("📄 getBooksWithFilter - page={}, size={}, sort={}, genre={}",
                filter.getPage(), filter.getSize(), filter.getSort(), filter.getGenre());

        List<Book> allBooks = bookRepository.findAll();
        List<Book> filteredBooks = applyFilters(allBooks, filter);
        List<Book> sortedBooks = applySorting(filteredBooks, filter.getSort());

        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;

        return createPageResponse(sortedBooks, page, size);
    }

    // ============================================================
    // 13. UPDATE BOOK WITH VERSION - OPTIMISTIC LOCK
    // ============================================================

    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class, OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @CachePut(value = "bookDetail", key = "#result.isbn", condition = "#result != null")
    @CacheEvict(value = {"bookList", "homepageFeatured", "topSellers"}, allEntries = true)
    @Transactional
    public BookResponse updateBookWithVersion(Long id, BookUpdateRequest request) {
        log.info("📝 [CACHE UPDATE] updateBookWithVersion - Updating book: id={}", id);

        Book existing = getBookEntityById(id);

        if (!existing.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateISBNException("ISBN " + request.getIsbn() + " already used by another book");
        }

        bookMapper.updateEntity(existing, request);
        BookValidator.validate(existing);

        Book updatedBook = bookRepository.save(existing);
        log.info("✅ Book updated successfully: id={}, version={}", updatedBook.getId(), updatedBook.getVersion());
        return bookMapper.toResponse(updatedBook);
    }

    // ============================================================
    // 14. UPDATE STOCK - UPDATE CACHE + EVICT
    // ============================================================

    @CachePut(value = "bookDetail", key = "#result.isbn", condition = "#result != null")
    @CacheEvict(value = {"bookList", "homepageFeatured", "topSellers"}, allEntries = true)
    @Transactional
    public BookResponse updateStock(Long id, int quantity) {
        log.info("📦 updateStock - Updating stock: id={}, quantity={}", id, quantity);

        Book book = getBookEntityById(id);
        int newStock = book.getStock() - quantity;

        if (newStock < 0) {
            throw new IllegalArgumentException("Not enough stock for book with id: " + id);
        }

        book.setStock(newStock);
        Book updatedBook = bookRepository.save(book);
        log.info("✅ Stock updated: id={}, newStock={}", id, newStock);
        return bookMapper.toResponse(updatedBook);
    }

    // ============================================================
    // 15. CLEAR ALL CACHE
    // ============================================================

    @CacheEvict(value = {"bookDetail", "bookList", "topSellers", "homepageFeatured"}, allEntries = true)
    public void clearAllBookCache() {
        log.info("🗑️ [CACHE EVICT] Clearing all book-related caches");
    }

    // ============================================================
    // 16. PRIVATE HELPER METHODS
    // ============================================================

    private List<Book> applyFilters(List<Book> books, BookFilterRequest filter) {
        return books.stream()
                .filter(book -> filterByGenre(book, filter.getGenre()))
                .filter(book -> filterByPrice(book, filter.getMinPrice(), filter.getMaxPrice()))
                .collect(Collectors.toList());
    }

    private boolean filterByGenre(Book book, String genre) {
        if (genre == null || genre.isBlank()) {
            return true;
        }
        if (book.getCategories() == null || book.getCategories().isEmpty()) {
            return false;
        }
        return book.getCategories().stream()
                .anyMatch(category -> category.getName().equalsIgnoreCase(genre));
    }

    private boolean filterByPrice(Book book, BigDecimal minPrice, BigDecimal maxPrice) {
        if (book.getPrice() == null) {
            return false;
        }
        boolean minCondition = minPrice == null || book.getPrice().compareTo(minPrice) >= 0;
        boolean maxCondition = maxPrice == null || book.getPrice().compareTo(maxPrice) <= 0;
        return minCondition && maxCondition;
    }

    private List<Book> applySorting(List<Book> books, String sort) {
        if (sort == null || sort.isBlank()) {
            return books;
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

    private PageResponse<BookSummaryResponse> createPageResponse(List<Book> books, int page, int size) {
        int total = books.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<Book> pageContent = fromIndex < total ? books.subList(fromIndex, toIndex) : List.of();

        return PageResponse.<BookSummaryResponse>builder()
                .content(bookMapper.toSummaryList(pageContent))
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
}