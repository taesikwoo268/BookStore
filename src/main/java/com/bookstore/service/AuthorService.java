package com.bookstore.service;

import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Author;
import com.bookstore.model.Book;
import com.bookstore.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final BookService bookService;

    public Author createAuthor(Author author) {
        return authorRepository.save(author);
    }

    public Author updateAuthor(Long id, Author updatedAuthor) {
        Author existing = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        existing.setName(updatedAuthor.getName());
        existing.setBiography(updatedAuthor.getBiography());
        return authorRepository.save(existing);
    }

    public void deleteAuthor(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }
        authorRepository.deleteById(id);
    }

    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public List<Author> searchAuthors(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllAuthors();
        }
        String lowerKeyword = keyword.toLowerCase();
        return authorRepository.findAll().stream()
                .filter(a -> a.getName().toLowerCase().contains(lowerKeyword) ||
                        (a.getBiography() != null && a.getBiography().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }

    public List<Book> getBooksByAuthor(Long authorId) {
        Author author = getAuthorById(authorId);
        return bookService.getAllBooks().stream()
                .filter(b -> b.getAuthors() != null && b.getAuthors().stream().map(Author::getId).anyMatch(id -> id.equals(authorId)))
                .collect(Collectors.toList());
    }
}