package com.bookstore.repository;

import com.bookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Override
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.categories " +
            "LEFT JOIN FETCH b.author")
    List<Book> findAll();
    boolean existsByIsbn(String isbn);
    Optional<Book> findByIsbn(String isbn);
}