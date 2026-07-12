package com.bookstore.controller;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookController bookController;

    private Book book;
    private BookCreateRequest createRequest;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .id(1L)
                .isbn("978-0439708184")
                .title("Harry Potter")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .build();

        createRequest = BookCreateRequest.builder()
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        bookResponse = BookResponse.builder()
                .id(1L)
                .isbn("978-0439708184")
                .title("Harry Potter")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .build();
    }

    @Test
    void createBook_ShouldReturnCreated() {
        when(bookService.createBook(any(BookCreateRequest.class))).thenReturn(book);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(bookResponse);

        ApiResponse<BookResponse> response = bookController.createBook(createRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Book created successfully", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void getBookById_ShouldReturnBook() {
        when(bookService.getBookById(1L)).thenReturn(book);
        when(bookMapper.toDetailResponse(any(Book.class))).thenReturn(null);

        ApiResponse<?> response = bookController.getBookById(1L);

        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    void getAllBooks_ShouldReturnList() {
        when(bookService.getAllBooks()).thenReturn(List.of(book));
        when(bookMapper.toSummaryList(anyList())).thenReturn(List.of());

        ApiResponse<?> response = bookController.getAllBooks();

        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    void deleteBook_ShouldReturnSuccess() {
        doNothing().when(bookService).deleteBook(1L);

        ApiResponse<Void> response = bookController.deleteBook(1L);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Book deleted successfully", response.getMessage());
        verify(bookService).deleteBook(1L);
    }
}