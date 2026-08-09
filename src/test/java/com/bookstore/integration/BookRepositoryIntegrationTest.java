//package com.bookstore.integration;
//
//import com.bookstore.model.Author;
//import com.bookstore.model.Book;
//import com.bookstore.model.Category;
//import com.bookstore.repository.BookRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ActiveProfiles("test")
//@DisplayName("BookRepository Integration Tests")
//class BookRepositoryIntegrationTest extends BaseIntegrationTest {
//
//    @Autowired
//    private BookRepository bookRepository;
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    private Author testAuthor;
//    private Category testCategory;
//    private Book testBook;
//
//    @BeforeEach
//    void setUp() {
//        testAuthor = Author.builder()
//                .name("J.K. Rowling")
//                .biography("British author")
//                .build();
//        entityManager.persist(testAuthor);
//
//        testCategory = Category.builder()
//                .name("Fantasy")
//                .description("Magic books")
//                .build();
//        entityManager.persist(testCategory);
//
//        testBook = Book.builder()
//                .isbn("978-0439708184")
//                .title("Harry Potter")
//                .price(new BigDecimal("19.99"))
//                .stock(100)
//                .salesCount(0)
//                .author(testAuthor)
//                .categories(List.of(testCategory))
//                .version(0)
//                .build();
//
//        entityManager.persist(testBook);
//        entityManager.flush();
//    }
//
//    @Test
//    @DisplayName("save - should save a book")
//    void save_shouldSaveBook() {
//        Book newBook = Book.builder()
//                .isbn("978-1234567890")
//                .title("Test Book")
//                .price(new BigDecimal("29.99"))
//                .stock(50)
//                .salesCount(0)
//                .author(testAuthor)
//                .categories(List.of(testCategory))
//                .version(0)
//                .build();
//
//        Book savedBook = bookRepository.save(newBook);
//        entityManager.flush();
//
//        assertThat(savedBook.getId()).isNotNull();
//        assertThat(savedBook.getIsbn()).isEqualTo("978-1234567890");
//        assertThat(savedBook.getTitle()).isEqualTo("Test Book");
//    }
//
//    @Test
//    @DisplayName("findById - should return book by id")
//    void findById_shouldReturnBookById() {
//        Optional<Book> found = bookRepository.findById(testBook.getId());
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getId()).isEqualTo(testBook.getId());
//    }
//
//    @Test
//    @DisplayName("findByIsbn - should return book by isbn")
//    void findByIsbn_shouldReturnBookByIsbn() {
//        Optional<Book> found = bookRepository.findByIsbn("978-0439708184");
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getIsbn()).isEqualTo("978-0439708184");
//    }
//
//    @Test
//    @DisplayName("findAll - should return all books")
//    void findAll_shouldReturnAllBooks() {
//        Book book2 = Book.builder()
//                .isbn("978-0618260256")
//                .title("The Hobbit")
//                .price(new BigDecimal("14.99"))
//                .stock(120)
//                .salesCount(0)
//                .author(testAuthor)
//                .categories(List.of(testCategory))
//                .version(0)
//                .build();
//        bookRepository.save(book2);
//        entityManager.flush();
//
//        List<Book> books = bookRepository.findAll();
//
//        assertThat(books).hasSize(2);
//        assertThat(books).extracting(Book::getTitle)
//                .containsExactlyInAnyOrder("Harry Potter", "The Hobbit");
//    }
//
//    @Test
//    @DisplayName("existsByIsbn - should return true for existing isbn")
//    void existsByIsbn_shouldReturnTrueForExistingIsbn() {
//        boolean exists = bookRepository.existsByIsbn("978-0439708184");
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    @DisplayName("deleteById - should delete book by id")
//    void deleteById_shouldDeleteBookById() {
//        bookRepository.deleteById(testBook.getId());
//        entityManager.flush();
//
//        Optional<Book> found = bookRepository.findById(testBook.getId());
//        assertThat(found).isEmpty();
//    }
//
//    @Test
//    @DisplayName("findByAuthorId - should return books by author")
//    void findByAuthorId_shouldReturnBooksByAuthor() {
//        Book book2 = Book.builder()
//                .isbn("978-0439064873")
//                .title("Harry Potter 2")
//                .price(new BigDecimal("20.99"))
//                .stock(85)
//                .salesCount(0)
//                .author(testAuthor)
//                .categories(List.of(testCategory))
//                .version(0)
//                .build();
//        bookRepository.save(book2);
//        entityManager.flush();
//
//        List<Book> books = bookRepository.findByAuthorId(testAuthor.getId());
//
//        assertThat(books).hasSize(2);
//        assertThat(books).extracting(Book::getTitle)
//                .containsExactlyInAnyOrder("Harry Potter", "Harry Potter 2");
//    }
//
//    @Test
//    @DisplayName("findByCategoriesId - should return books by category")
//    void findByCategoriesId_shouldReturnBooksByCategory() {
//        Category sciFi = Category.builder()
//                .name("Science Fiction")
//                .description("Sci-Fi books")
//                .build();
//        entityManager.persist(sciFi);
//
//        Book book2 = Book.builder()
//                .isbn("978-0451524935")
//                .title("1984")
//                .price(new BigDecimal("13.99"))
//                .stock(95)
//                .salesCount(0)
//                .author(testAuthor)
//                .categories(List.of(sciFi))
//                .version(0)
//                .build();
//        bookRepository.save(book2);
//        entityManager.flush();
//
//        List<Book> fantasyBooks = bookRepository.findByCategoriesId(testCategory.getId());
//
//        assertThat(fantasyBooks).hasSize(1);
//        assertThat(fantasyBooks.get(0).getTitle()).isEqualTo("Harry Potter");
//    }
//}