package com.bookstore.repository;

import com.bookstore.model.Book;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryBookRepository implements BookRepository {
    private final Map<Long, Book> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(idGenerator.getAndIncrement());
        }
        store.put(book.getId(), book);
        return book;
    }

    @Override
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return store.values().stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst();
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return store.values().stream().anyMatch(b -> b.getIsbn().equals(isbn));
    }
}