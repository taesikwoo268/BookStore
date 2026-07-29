package com.bookstore.service;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateISBNException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Author;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.BookRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    private BookCreateRequest createRequest;
    private BookUpdateRequest updateRequest;
    private Author author;
    private Category category;
    private List<Category> categories;

    @BeforeEach
    void setUp() {
        author = Author.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("British author")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Fantasy")
                .description("Magic books")
                .build();
        categories = List.of(category);

        book = Book.builder()
                .id(1L)
                .isbn("978-0439708184")
                .title("Harry Potter")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .author(author)
                .categories(categories)
                .version(0)
                .build();

        createRequest = BookCreateRequest.builder()
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

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
    @DisplayName("createBook - valid input returns Book")
    void createBook_validInput_returnsBook() {
        when(bookMapper.toEntity(any(BookCreateRequest.class))).thenReturn(book);
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.createBook(createRequest);

        assertNotNull(result);
        assertEquals("Harry Potter", result.getTitle());
        assertEquals("978-0439708184", result.getIsbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook - duplicate ISBN throws DuplicateISBNException")
    void createBook_duplicateISBN_throwsException() {
        when(bookMapper.toEntity(any(BookCreateRequest.class))).thenReturn(book);
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(createRequest))
                .isInstanceOf(DuplicateISBNException.class);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook - with null stock sets default salesCount to 0")
    void createBook_nullStock_setsSalesCountToZero() {
        Book bookWithStock = Book.builder()
                .id(1L)
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(0)
                .salesCount(0)
                .author(author)
                .categories(categories)
                .build();

        when(bookMapper.toEntity(any(BookCreateRequest.class))).thenReturn(bookWithStock);
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(bookWithStock);

        Book result = bookService.createBook(createRequest);

        assertNotNull(result);
        assertEquals(0, result.getSalesCount());
    }

    // ============================================================
    // 2. GET BOOK TESTS
    // ============================================================

    @Test
    @DisplayName("getBookById - valid id returns Book")
    void getBookById_validId_returnsBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals("Harry Potter", result.getTitle());
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
    @DisplayName("getAllBooks - returns list of books")
    void getAllBooks_returnsListOfBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.getAllBooks();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getAllBooks - returns empty list when no books")
    void getAllBooks_returnsEmptyList() {
        when(bookRepository.findAll()).thenReturn(List.of());

        List<Book> result = bookService.getAllBooks();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ============================================================
    // 3. UPDATE BOOK TESTS
    // ============================================================

    @Test
    @DisplayName("updateBook - valid input returns updated Book")
    void updateBook_validInput_returnsUpdatedBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.updateBook(1L, updateRequest);

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
    @DisplayName("updateBook - duplicate ISBN throws DuplicateISBNException")
    void updateBook_duplicateISBN_throwsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

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

    // ============================================================
    // 5. SEARCH & FILTER TESTS
    // ============================================================

    @Test
    @DisplayName("searchBook - with keyword returns matching books")
    void searchBook_withKeyword_returnsMatchingBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.searchBook("Harry");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("filterByPrice - returns books in price range")
    void filterByPrice_returnsBooksInRange() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.filterByPrice(new BigDecimal("10"), new BigDecimal("20"));

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ============================================================
    // 6. UPDATE STOCK TESTS
    // ============================================================

    @Test
    @DisplayName("updateStock - valid quantity updates stock")
    void updateStock_validQuantity_updatesStock() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.updateStock(1L, 10);

        assertNotNull(result);
        verify(bookRepository).save(any(Book.class));
    }
}