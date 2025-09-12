package com.expense.service;

import com.expense.exception.CategoryInUseException;
import com.expense.model.Category;
import com.expense.repository.CategoryRepository;
import com.expense.repository.ExpenseRepository;
import com.expense.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, ExpenseRepository expenseRepository, UserService userService) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    public List<String> getCategoryNames() {
        Long userId = userService.getCurrentUserId();
        return categoryRepository.findAllByUserId(userId).stream()
                .filter(java.util.Objects::nonNull) // Add null check
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
    }

    public void deleteCategory(String categoryName) {
        Long userId = userService.getCurrentUserId();
        long usageCount = expenseRepository.countByCategoryCategoryNameAndUserId(categoryName, userId);
        if (usageCount > 0) {
            throw new CategoryInUseException("This category cannot be deleted as Expense is present for this category.");
        }
        categoryRepository.findAllByUserId(userId).stream()
                .filter(category -> category.getCategoryName().equalsIgnoreCase(categoryName))
                .findFirst()
                .ifPresent(categoryRepository::delete);
    }

    public java.util.Optional<Category> findCategoryByName(String categoryName) {
        Long userId = userService.getCurrentUserId();
        return categoryRepository.findByCategoryNameAndUserId(categoryName, userId);
    }

    public void addCategory(String categoryName) {
        Long userId = userService.getCurrentUserId();
        boolean categoryExists = categoryRepository.findAllByUserId(userId).stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(categoryName));

        if (!categoryExists) {
            Category newCategory = new Category(categoryName, userId);
            categoryRepository.save(newCategory);
        }
    }
}
