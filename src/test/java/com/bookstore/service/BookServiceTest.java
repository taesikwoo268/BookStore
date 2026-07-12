package com.bookstore.service;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateISBNException;
import com.bookstore.model.Author;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorService authorService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private BookCreateRequest createRequest;
    private BookUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .id(1L)
                .isbn("978-0439708184")
                .title("Harry Potter")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
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
                .build();
    }

    // ===== CREATE TESTS =====

    @Test
    void createBook_ShouldSaveSuccessfully() {
        when(authorService.getAuthorById(anyLong())).thenReturn(new Author());
        when(categoryService.getCategoriesByIds(anyList())).thenReturn(List.of(new Category()));
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.createBook(createRequest);

        assertNotNull(result);
        assertEquals("Harry Potter", result.getTitle());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_DuplicateISBN_ShouldThrowException() {
        when(authorService.getAuthorById(anyLong())).thenReturn(new Author());
        when(categoryService.getCategoriesByIds(anyList())).thenReturn(List.of(new Category()));
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        assertThrows(DuplicateISBNException.class, () -> bookService.createBook(createRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    // ===== GET TESTS =====

    @Test
    void getBookById_ShouldReturnBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals("Harry Potter", result.getTitle());
    }

    @Test
    void getBookById_NotFound_ShouldThrowException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBookById(999L));
    }

    @Test
    void getAllBooks_ShouldReturnList() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.getAllBooks();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ===== UPDATE TESTS =====

    @Test
    void updateBook_ShouldUpdateSuccessfully() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.updateBook(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Harry Potter", result.getTitle());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_NotFound_ShouldThrowException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.updateBook(999L, updateRequest));
    }

    // ===== DELETE TESTS =====

    @Test
    void deleteBook_ShouldDeleteSuccessfully() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteBook_NotFound_ShouldThrowException() {
        when(bookRepository.existsById(999L)).thenReturn(false);

        assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(999L));
    }

    // ===== SEARCH TESTS =====

    @Test
    void searchBook_WithKeyword_ShouldReturnMatchingBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.searchBook("Harry");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(b -> b.getTitle().contains("Harry")));
    }

    @Test
    void searchBook_WithNullKeyword_ShouldReturnAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.searchBook(null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ===== FILTER TESTS =====

    @Test
    void filterByPrice_ShouldReturnBooksInRange() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.filterByPrice(new BigDecimal("10"), new BigDecimal("20"));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(b ->
                b.getPrice().compareTo(new BigDecimal("10")) >= 0 &&
                        b.getPrice().compareTo(new BigDecimal("20")) <= 0
        ));
    }

    // ===== TOP 5 TESTS =====

    @Test
    void top5BestSellers_ShouldReturnTop5() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.top5BestSellers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}