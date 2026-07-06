package com.bookstore.service;

import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final BookService bookService;

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());
        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllCategories();
        }
        String lowerKeyword = keyword.toLowerCase();
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(lowerKeyword) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }

    public List<Book> getBooksByCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        return bookService.getAllBooks().stream()
                .filter(b -> b.getCategory() != null && b.getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());
    }
}