package com.bookstore.controller;

import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.CategoryDetailResponse;
import com.bookstore.dto.response.CategorySummaryResponse;
import com.bookstore.mapper.CategoryMapper;
import com.bookstore.model.Category;
import com.bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategorySummaryResponse> createCategory(@RequestBody Category category) {
        Category saved = categoryService.createCategory(category);
        return ApiResponse.success("Category created successfully", categoryMapper.toSummaryResponse(saved));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategorySummaryResponse> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updated = categoryService.updateCategory(id, category);
        return ApiResponse.success("Category updated successfully", categoryMapper.toSummaryResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success("Category deleted successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryDetailResponse> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ApiResponse.success(categoryMapper.toDetailResponse(category));
    }

    @GetMapping
    public ApiResponse<List<CategorySummaryResponse>> getAllCategories() {
        List<CategorySummaryResponse> categories = categoryMapper.toSummaryList(categoryService.getAllCategories());
        return ApiResponse.success(categories);
    }

    @GetMapping("/search")
    public ApiResponse<List<CategorySummaryResponse>> searchCategories(@RequestParam String keyword) {
        List<CategorySummaryResponse> categories = categoryMapper.toSummaryList(categoryService.searchCategories(keyword));
        return ApiResponse.success(categories);
    }
}