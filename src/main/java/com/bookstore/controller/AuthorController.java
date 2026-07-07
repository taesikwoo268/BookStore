package com.bookstore.controller;

import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.AuthorDetailResponse;
import com.bookstore.dto.response.AuthorSummaryResponse;
import com.bookstore.mapper.AuthorMapper;
import com.bookstore.model.Author;
import com.bookstore.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthorSummaryResponse> createAuthor(@RequestBody Author author) {
        Author saved = authorService.createAuthor(author);
        return ApiResponse.success("Author created successfully", authorMapper.toSummaryResponse(saved));
    }

    @PutMapping("/{id}")
    public ApiResponse<AuthorSummaryResponse> updateAuthor(@PathVariable Long id, @RequestBody Author author) {
        Author updated = authorService.updateAuthor(id, author);
        return ApiResponse.success("Author updated successfully", authorMapper.toSummaryResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ApiResponse.success("Author deleted successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<AuthorDetailResponse> getAuthorById(@PathVariable Long id) {
        Author author = authorService.getAuthorById(id);
        return ApiResponse.success(authorMapper.toDetailResponse(author));
    }

    @GetMapping
    public ApiResponse<List<AuthorSummaryResponse>> getAllAuthors() {
        List<AuthorSummaryResponse> authors = authorMapper.toSummaryList(authorService.getAllAuthors());
        return ApiResponse.success(authors);
    }

    @GetMapping("/search")
    public ApiResponse<List<AuthorSummaryResponse>> searchAuthors(@RequestParam String keyword) {
        List<AuthorSummaryResponse> authors = authorMapper.toSummaryList(authorService.searchAuthors(keyword));
        return ApiResponse.success(authors);
    }
}