package com.bookstore.controller;

import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.CategoryDetailResponse;
import com.bookstore.dto.response.CategorySummaryResponse;
import com.bookstore.mapper.CategoryMapper;
import com.bookstore.model.Category;
import com.bookstore.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "Create a new category", description = "Create a new category with the provided details")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategorySummaryResponse> createCategory(@RequestBody Category category) {
        Category saved = categoryService.createCategory(category);
        return ApiResponse.success("Category created successfully", categoryMapper.toSummaryResponse(saved));
    }

    @Operation(summary = "Update an existing category", description = "Update category details by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found")
    })
    @PutMapping("/{id}")
    public ApiResponse<CategorySummaryResponse> updateCategory(
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Category category) {
        Category updated = categoryService.updateCategory(id, category);
        return ApiResponse.success("Category updated successfully", categoryMapper.toSummaryResponse(updated));
    }

    @Operation(summary = "Delete a category", description = "Delete category by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Category deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success("Category deleted successfully");
    }

    @Operation(summary = "Get category by ID", description = "Retrieve detailed information of a category by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found")
    })
    @GetMapping("/{id}")
    public ApiResponse<CategoryDetailResponse> getCategoryById(
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ApiResponse.success(categoryMapper.toDetailResponse(category));
    }

    @Operation(summary = "Get all categories", description = "Retrieve a list of all categories")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ApiResponse<List<CategorySummaryResponse>> getAllCategories() {
        List<CategorySummaryResponse> categories = categoryMapper.toSummaryList(categoryService.getAllCategories());
        return ApiResponse.success(categories);
    }

    @Operation(summary = "Search categories by keyword", description = "Search categories by name or description")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/search")
    public ApiResponse<List<CategorySummaryResponse>> searchCategories(
            @Parameter(description = "Search keyword", required = true, example = "Fantasy")
            @RequestParam String keyword) {
        List<CategorySummaryResponse> categories = categoryMapper.toSummaryList(categoryService.searchCategories(keyword));
        return ApiResponse.success(categories);
    }
}