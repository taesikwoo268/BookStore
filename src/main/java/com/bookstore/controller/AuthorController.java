package com.bookstore.controller;

import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.AuthorDetailResponse;
import com.bookstore.dto.response.AuthorSummaryResponse;
import com.bookstore.mapper.AuthorMapper;
import com.bookstore.model.Author;
import com.bookstore.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Author Management", description = "APIs for managing authors")
public class AuthorController {
    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    @Operation(summary = "Create a new author", description = "Create a new author with the provided details")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Author created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthorSummaryResponse> createAuthor(@RequestBody Author author) {
        Author saved = authorService.createAuthor(author);
        return ApiResponse.success("Author created successfully", authorMapper.toSummaryResponse(saved));
    }

    @Operation(summary = "Update an existing author", description = "Update author details by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Author updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Author not found")
    })
    @PutMapping("/{id}")
    public ApiResponse<AuthorSummaryResponse> updateAuthor(
            @Parameter(description = "Author ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Author author) {
        Author updated = authorService.updateAuthor(id, author);
        return ApiResponse.success("Author updated successfully", authorMapper.toSummaryResponse(updated));
    }

    @Operation(summary = "Delete an author", description = "Delete author by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Author deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Author not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteAuthor(
            @Parameter(description = "Author ID", required = true, example = "1")
            @PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ApiResponse.success("Author deleted successfully");
    }

    @Operation(summary = "Get author by ID", description = "Retrieve detailed information of an author by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Author found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Author not found")
    })
    @GetMapping("/{id}")
    public ApiResponse<AuthorDetailResponse> getAuthorById(
            @Parameter(description = "Author ID", required = true, example = "1")
            @PathVariable Long id) {
        Author author = authorService.getAuthorById(id);
        return ApiResponse.success(authorMapper.toDetailResponse(author));
    }

    @Operation(summary = "Get all authors", description = "Retrieve a list of all authors")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ApiResponse<List<AuthorSummaryResponse>> getAllAuthors() {
        List<AuthorSummaryResponse> authors = authorMapper.toSummaryList(authorService.getAllAuthors());
        return ApiResponse.success(authors);
    }

    @Operation(summary = "Search authors by keyword", description = "Search authors by name or biography")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/search")
    public ApiResponse<List<AuthorSummaryResponse>> searchAuthors(
            @Parameter(description = "Search keyword", required = true, example = "Rowling")
            @RequestParam String keyword) {
        List<AuthorSummaryResponse> authors = authorMapper.toSummaryList(authorService.searchAuthors(keyword));
        return ApiResponse.success(authors);
    }
}