package com.bookstore.service;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookFilterRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.response.*;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateISBNException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Author;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.BookRepository;
import com.bookstore.validation.BookValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private BookResponse bookResponse;
    private BookDetailResponse bookDetailResponse;
    private BookSummaryResponse bookSummaryResponse;
    private BookCreateRequest createRequest;
    private BookUpdateRequest updateRequest;
    private Author author;
    private Category category;
    private List<Category> categories;
    private AuthorSummaryResponse authorSummaryResponse;
    private AuthorDetailResponse authorDetailResponse;
    private CategorySummaryResponse categorySummaryResponse;
    private CategoryDetailResponse categoryDetailResponse;

    @BeforeEach
    void setUp() {
        // ===== 1. Author =====
        author = Author.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("British author")
                .build();

        // ===== 2. Category =====
        category = Category.builder()
                .id(1L)
                .name("Fantasy")
                .description("Magic books")
                .build();
        categories = List.of(category);

        // ===== 3. AuthorSummaryResponse =====
        authorSummaryResponse = AuthorSummaryResponse.builder()
                .id(1L)
                .name("J.K. Rowling")
                .build();

        // ===== 4. AuthorDetailResponse =====
        authorDetailResponse = AuthorDetailResponse.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("British author")
                .build();

        // ===== 5. CategorySummaryResponse =====
        categorySummaryResponse = CategorySummaryResponse.builder()
                .id(1L)
                .name("Fantasy")
                .build();

        // ===== 6. CategoryDetailResponse =====
        categoryDetailResponse = CategoryDetailResponse.builder()
                .id(1L)
                .name("Fantasy")
                .description("Magic books")
                .build();

        // ===== 7. Book Entity =====
        book = Book.builder()
                .id(1L)
                .isbn("978-0439708184")
                .title("Harry Potter and the Sorcerer's Stone")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .author(author)
                .categories(categories)
                .version(0)
                .build();

        // ===== 8. BookResponse =====
        bookResponse = BookResponse.builder()
                .id(1L)
                .isbn("978-0439708184")
                .title("Harry Potter and the Sorcerer's Stone")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .author(authorSummaryResponse)
                .categories(List.of(categorySummaryResponse))
                .build();

        // ===== 9. BookDetailResponse =====
        bookDetailResponse = BookDetailResponse.builder()
                .id(1L)
                .isbn("978-0439708184")
                .title("Harry Potter and the Sorcerer's Stone")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .author(authorDetailResponse)
                .categories(List.of(categoryDetailResponse))
                .build();

        // ===== 10. BookSummaryResponse =====
        bookSummaryResponse = BookSummaryResponse.builder()
                .id(1L)
                .title("Harry Potter and the Sorcerer's Stone")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .authorName("J.K. Rowling")
                .categoryNames(List.of("Fantasy"))
                .build();

        // ===== 11. Create Request =====
        createRequest = BookCreateRequest.builder()
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // ===== 12. Update Request =====
        updateRequest = BookUpdateRequest.builder()
                .isbn("978-1234567890")
                .title("Updated Book")
                .price(new BigDecimal("29.99"))
                .stock(50)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();
    }

    // ============================================================
    // 1. CREATE BOOK TESTS
    // ============================================================

    @Test
    @DisplayName("createBook - valid input returns BookResponse")
    void createBook_validInput_returnsBookResponse() {
        when(bookMapper.toEntity(createRequest)).thenReturn(book);
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(bookResponse);

        BookResponse result = bookService.createBook(createRequest);

        assertNotNull(result);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook - duplicate ISBN throws DuplicateISBNException")
    void createBook_duplicateISBN_throwsException() {
        when(bookMapper.toEntity(createRequest)).thenReturn(book);
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(createRequest))
                .isInstanceOf(DuplicateISBNException.class);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook - null stock should throw IllegalArgumentException")
    void createBook_nullStock_throwsException() {
        Book bookWithNullStock = Book.builder()
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(null)
                .salesCount(0)
                .author(author)
                .categories(categories)
                .build();

        when(bookMapper.toEntity(createRequest)).thenReturn(bookWithNullStock);

        assertThatThrownBy(() -> bookService.createBook(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock must be non-negative");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook - negative stock should throw IllegalArgumentException")
    void createBook_negativeStock_throwsException() {
        Book bookWithNegativeStock = Book.builder()
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(-5)
                .salesCount(0)
                .author(author)
                .categories(categories)
                .build();

        when(bookMapper.toEntity(createRequest)).thenReturn(bookWithNegativeStock);

        assertThatThrownBy(() -> bookService.createBook(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock must be non-negative");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook - valid stock initializes salesCount")
    void createBook_validStock_initializesSalesCount() {
        Book bookWithValidStock = Book.builder()
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(50)
                .salesCount(null)
                .author(author)
                .categories(categories)
                .build();

        when(bookMapper.toEntity(createRequest)).thenReturn(bookWithValidStock);
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(bookResponse);

        BookResponse result = bookService.createBook(createRequest);

        assertNotNull(result);
        assertEquals(0, result.getSalesCount());
        verify(bookRepository).save(any(Book.class));
    }


    // ============================================================
    // 2. GET BOOK TESTS
    // ============================================================

    @Test
    @DisplayName("getAllBooks - returns list of BookSummaryResponse")
    void getAllBooks_returnsListOfBookSummaryResponse() {
        List<Book> books = List.of(book);
        List<BookSummaryResponse> expected = List.of(bookSummaryResponse);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toSummaryList(anyList())).thenReturn(expected);

        List<BookSummaryResponse> result = bookService.getAllBooks();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookRepository).findAll();
        verify(bookMapper).toSummaryList(anyList());
    }

    @Test
    @DisplayName("getAllBooks - returns empty list when no books")
    void getAllBooks_returnsEmptyList() {
        when(bookRepository.findAll()).thenReturn(List.of());
        when(bookMapper.toSummaryList(anyList())).thenReturn(List.of());

        List<BookSummaryResponse> result = bookService.getAllBooks();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getBookById - valid id returns BookDetailResponse")
    void getBookById_validId_returnsBookDetailResponse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toDetailResponse(any(Book.class))).thenReturn(bookDetailResponse);

        BookDetailResponse result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals("Harry Potter and the Sorcerer's Stone", result.getTitle());
        verify(bookRepository).findById(1L);
    }

    @Test
    @DisplayName("getBookById - invalid id throws BookNotFoundException")
    void getBookById_invalidId_throwsException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(999L))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("getBookByIsbn - valid isbn returns BookDetailResponse")
    void getBookByIsbn_validIsbn_returnsBookDetailResponse() {
        when(bookRepository.findByIsbn("978-0439708184")).thenReturn(Optional.of(book));
        when(bookMapper.toDetailResponse(any(Book.class))).thenReturn(bookDetailResponse);

        BookDetailResponse result = bookService.getBookByIsbn("978-0439708184");

        assertNotNull(result);
        assertEquals("978-0439708184", result.getIsbn());
        verify(bookRepository).findByIsbn("978-0439708184");
    }

    @Test
    @DisplayName("getBookByIsbn - invalid isbn throws BookNotFoundException")
    void getBookByIsbn_invalidIsbn_throwsException() {
        when(bookRepository.findByIsbn("invalid-isbn")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookByIsbn("invalid-isbn"))
                .isInstanceOf(BookNotFoundException.class);
    }

    // ============================================================
    // 3. UPDATE BOOK TESTS
    // ============================================================

    @Test
    @DisplayName("updateBook - valid input returns updated BookResponse")
    void updateBook_validInput_returnsUpdatedBookResponse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(bookResponse);

        BookResponse result = bookService.updateBook(1L, updateRequest);

        assertNotNull(result);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("updateBook - book not found throws BookNotFoundException")
    void updateBook_bookNotFound_throwsException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(999L, updateRequest))
                .isInstanceOf(BookNotFoundException.class);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("updateBook - duplicate ISBN (excluding itself) throws DuplicateISBNException")
    void updateBook_duplicateISBN_throwsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn("978-1234567890")).thenReturn(true);

        assertThatThrownBy(() -> bookService.updateBook(1L, updateRequest))
                .isInstanceOf(DuplicateISBNException.class);
        verify(bookRepository, never()).save(any(Book.class));
    }

    // ============================================================
    // 4. DELETE BOOK TESTS
    // ============================================================

    @Test
    @DisplayName("deleteBook - valid id deletes successfully")
    void deleteBook_validId_deletesSuccessfully() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteBook - book not found throws BookNotFoundException")
    void deleteBook_bookNotFound_throwsException() {
        when(bookRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(999L))
                .isInstanceOf(BookNotFoundException.class);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteBookByIsbn - valid isbn deletes successfully")
    void deleteBookByIsbn_validIsbn_deletesSuccessfully() {
        when(bookRepository.findByIsbn("978-0439708184")).thenReturn(Optional.of(book));

        bookService.deleteBookByIsbn("978-0439708184");

        verify(bookRepository).delete(book);
    }

    @Test
    @DisplayName("deleteBookByIsbn - invalid isbn throws BookNotFoundException")
    void deleteBookByIsbn_invalidIsbn_throwsException() {
        when(bookRepository.findByIsbn("invalid-isbn")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBookByIsbn("invalid-isbn"))
                .isInstanceOf(BookNotFoundException.class);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    // ============================================================
    // 5. SEARCH BOOK TESTS
    // ============================================================

    @Test
    @DisplayName("searchBook - with keyword returns matching books")
    void searchBook_withKeyword_returnsMatchingBooks() {
        List<Book> books = List.of(book);
        List<BookSummaryResponse> expected = List.of(bookSummaryResponse);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toSummaryList(anyList())).thenReturn(expected);

        List<BookSummaryResponse> result = bookService.searchBook("Harry");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Harry Potter and the Sorcerer's Stone", result.get(0).getTitle());
    }

    @Test
    @DisplayName("searchBook - with null keyword returns all books")
    void searchBook_withNullKeyword_returnsAllBooks() {
        List<Book> books = List.of(book);
        List<BookSummaryResponse> expected = List.of(bookSummaryResponse);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toSummaryList(anyList())).thenReturn(expected);

        List<BookSummaryResponse> result = bookService.searchBook(null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchBook - with empty keyword returns all books")
    void searchBook_withEmptyKeyword_returnsAllBooks() {
        List<Book> books = List.of(book);
        List<BookSummaryResponse> expected = List.of(bookSummaryResponse);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toSummaryList(anyList())).thenReturn(expected);

        List<BookSummaryResponse> result = bookService.searchBook("");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchBook - with no matching keyword returns empty list")
    void searchBook_noMatchingKeyword_returnsEmptyList() {
        List<Book> books = List.of(book);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toSummaryList(anyList())).thenReturn(List.of());

        List<BookSummaryResponse> result = bookService.searchBook("xyz");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ============================================================
    // 6. FILTER BY PRICE TESTS
    // ============================================================

    @Test
    @DisplayName("filterByPrice - returns books in price range")
    void filterByPrice_returnsBooksInRange() {
        List<Book> books = List.of(book);
        List<BookSummaryResponse> expected = List.of(bookSummaryResponse);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toSummaryList(anyList())).thenReturn(expected);

        List<BookSummaryResponse> result = bookService.filterByPrice(
                new BigDecimal("10"), new BigDecimal("20"));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("filterByPrice - no books in range returns empty list")
    void filterByPrice_noBooksInRange_returnsEmptyList() {
        List<Book> books = List.of(book);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toSummaryList(anyList())).thenReturn(List.of());

        List<BookSummaryResponse> result = bookService.filterByPrice(
                new BigDecimal("50"), new BigDecimal("100"));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ============================================================
    // 7. TOP 5 BEST SELLERS TESTS
    // ============================================================

    @Test
    @DisplayName("top5BestSellers - returns sorted list")
    void top5BestSellers_returnsSortedList() {
        Book book1 = Book.builder().id(1L).title("Book 1").salesCount(100).build();
        Book book2 = Book.builder().id(2L).title("Book 2").salesCount(200).build();
        Book book3 = Book.builder().id(3L).title("Book 3").salesCount(150).build();

        List<Book> books = List.of(book1, book2, book3);
        List<BookSummaryResponse> expected = List.of(
                BookSummaryResponse.builder().id(2L).title("Book 2").build(),
                BookSummaryResponse.builder().id(3L).title("Book 3").build(),
                BookSummaryResponse.builder().id(1L).title("Book 1").build()
        );

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toSummaryList(anyList())).thenReturn(expected);

        List<BookSummaryResponse> result = bookService.top5BestSellers();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Book 2", result.get(0).getTitle());
    }

    // ============================================================
    // 8. GROUP BY CATEGORY TESTS
    // ============================================================

    @Test
    @DisplayName("groupByCategory - returns map of category to books")
    void groupByCategory_returnsMap() {
        // Given
        Category fantasy = Category.builder().id(1L).name("Fantasy").build();
        Category sciFi = Category.builder().id(2L).name("Sci-Fi").build();

        Book book1 = Book.builder().id(1L).title("Book 1").categories(List.of(fantasy)).build();
        Book book2 = Book.builder().id(2L).title("Book 2").categories(List.of(fantasy)).build();
        Book book3 = Book.builder().id(3L).title("Book 3").categories(List.of(sciFi)).build();

        // Mock cho bookMapper.toSummaryList cho từng category
        when(bookRepository.findAll()).thenReturn(List.of(book1, book2, book3));
        when(bookMapper.toSummaryList(List.of(book1, book2))).thenReturn(
                List.of(
                        BookSummaryResponse.builder().id(1L).title("Book 1").build(),
                        BookSummaryResponse.builder().id(2L).title("Book 2").build()
                )
        );
        when(bookMapper.toSummaryList(List.of(book3))).thenReturn(
                List.of(
                        BookSummaryResponse.builder().id(3L).title("Book 3").build()
                )
        );

        // When
        var result = bookService.groupByCategory();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("Fantasy"));
        assertTrue(result.containsKey("Sci-Fi"));
        assertEquals(2, result.get("Fantasy").size());
        assertEquals(1, result.get("Sci-Fi").size());
    }

    @Test
    @DisplayName("groupByCategory - filters books with no category")
    void groupByCategory_filtersBooksWithNoCategory() {
        // Given
        Category fantasy = Category.builder().id(1L).name("Fantasy").build();
        Book book1 = Book.builder().id(1L).title("Book 1").categories(List.of(fantasy)).build();
        Book book2 = Book.builder().id(2L).title("Book 2").categories(null).build();
        Book book3 = Book.builder().id(3L).title("Book 3").categories(List.of()).build();

        when(bookRepository.findAll()).thenReturn(List.of(book1, book2, book3));

        // Khi gọi groupByCategory, bookMapper sẽ được gọi với books từ fantasy
        when(bookMapper.toSummaryList(List.of(book1))).thenReturn(
                List.of(BookSummaryResponse.builder().id(1L).title("Book 1").build())
        );

        // When
        var result = bookService.groupByCategory();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("Fantasy"));
        assertEquals(1, result.get("Fantasy").size());
    }

    // ============================================================
    // 9. GET BOOKS WITH FILTER TESTS
    // ============================================================

    @Test
    @DisplayName("getBooksWithFilter - returns paginated books")
    void getBooksWithFilter_returnsPaginatedBooks() {
        List<Book> books = List.of(book, book, book, book, book);
        BookFilterRequest filter = BookFilterRequest.builder()
                .page(0)
                .size(2)
                .build();

        when(bookRepository.findAll()).thenReturn(books);

        PageResponse<BookSummaryResponse> result = bookService.getBooksWithFilter(filter);

        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(2, result.getPageSize());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }

    // ============================================================
    // 10. UPDATE STOCK TESTS
    // ============================================================

    @Test
    @DisplayName("updateStock - valid quantity updates stock")
    void updateStock_validQuantity_updatesStock() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(bookResponse);

        BookResponse result = bookService.updateStock(1L, 10);

        assertNotNull(result);
        assertEquals(90, book.getStock());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("updateStock - invalid quantity throws exception")
    void updateStock_invalidQuantity_throwsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.updateStock(1L, 150))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not enough stock");
        verify(bookRepository, never()).save(any(Book.class));
    }

    // ============================================================
    // 11. CLEAR CACHE TESTS
    // ============================================================

    @Test
    @DisplayName("clearAllBookCache - clears all book caches")
    void clearAllBookCache_clearsAllBookCaches() {
        bookService.clearAllBookCache();
        // Không có verify cụ thể, chỉ kiểm tra không throw exception
    }

    // ============================================================
    // 12. VERIFY REPOSITORY INTERACTIONS
    // ============================================================

    @Test
    @DisplayName("createBook - verifies repository interactions")
    void createBook_verifiesRepositoryInteractions() {
        when(bookMapper.toEntity(createRequest)).thenReturn(book);
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(bookResponse);

        bookService.createBook(createRequest);

        verify(bookRepository).existsByIsbn(anyString());
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toEntity(createRequest);
        verify(bookMapper).toResponse(any(Book.class));
    }
}