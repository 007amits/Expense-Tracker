package com.expense.service;

import com.expense.model.Category;
import com.expense.model.Expense;
import com.expense.expensetracker.service.UserService;
import com.expense.util.DateValidator;
import com.expense.repository.CategoryRepository;
import com.expense.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, CategoryService categoryService, UserService userService, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
    }





    public List<Expense> getAllExpenses(String category, String period, String startDate, String endDate, String sortOrder) {
        Long userId = userService.getCurrentUserId();
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        if (StringUtils.hasText(category)) {
            expenses = expenses.stream()
                    .filter(expense -> category.equalsIgnoreCase(expense.getCategory().getCategoryName()))
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(period) && !"all".equalsIgnoreCase(period)) {
            final LocalDate start, end;
            if ("custom".equalsIgnoreCase(period)) {
                start = StringUtils.hasText(startDate) ? LocalDate.parse(startDate) : LocalDate.MIN;
                end = StringUtils.hasText(endDate) ? LocalDate.parse(endDate) : LocalDate.MAX;
            } else {
                LocalDate today = LocalDate.now();
                end = today;
                switch (period.toLowerCase()) {
                    case "weekly":
                        start = today.with(DayOfWeek.MONDAY);
                        break;
                    case "monthly":
                        start = today.withDayOfMonth(1);
                        break;
                    case "daily":
                    default:
                        start = today;
                        break;
                }
            }
            expenses = expenses.stream()
                    .filter(expense -> !expense.getDate().isBefore(start) && !expense.getDate().isAfter(end))
                    .collect(Collectors.toList());
        }

        if ("asc".equalsIgnoreCase(sortOrder)) {
            expenses.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));
        } else {
            expenses.sort((e1, e2) -> e2.getDate().compareTo(e1.getDate()));
        }
        return expenses;
    }

    public Expense getExpenseById(Long id) {
        Long userId = userService.getCurrentUserId();
        return expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }

    public Expense createExpense(Expense expense) {
        // Validate that the date is not in the future
        DateValidator.validateDateNotInFuture(expense.getDate());
        
        Long userId = userService.getCurrentUserId();
        expense.setUserId(userId);

        if (expense.getCategory() != null) {
            Category category = categoryService.findCategoryByName(expense.getCategory().getCategoryName())
                    .orElseGet(() -> {
                        Category newCategory = new Category(expense.getCategory().getCategoryName(), userId);
                        return categoryRepository.save(newCategory);
                    });
            expense.setCategory(category);
        }

        return expenseRepository.save(expense);
    }

    /**
     * Get the total amount of all expenses for the current user
     * @return Total expenses as BigDecimal
     */
    public BigDecimal getTotalExpenses() {
        Long userId = userService.getCurrentUserId();
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public Expense updateExpense(Long id, Expense expenseDetails) {
        // Validate that the date is not in the future
        DateValidator.validateDateNotInFuture(expenseDetails.getDate());
        
        Long userId = userService.getCurrentUserId();
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        expense.setDescription(expenseDetails.getDescription());
        expense.setAmount(expenseDetails.getAmount());
        expense.setDate(expenseDetails.getDate());
        expense.setPaymentMode(expenseDetails.getPaymentMode());
        expense.setNote(expenseDetails.getNote());
        expense.setCurrency(expenseDetails.getCurrency());

        if (expenseDetails.getCategory()!=null) {
            Category category = categoryService.findCategoryByName(expenseDetails.getCategory().getCategoryName())
                    .orElseGet(() -> {
                        Category newCategory = new Category(expenseDetails.getCategory().getCategoryName(), userService.getCurrentUserId());
                        return categoryRepository.save(newCategory);
                    });
            expense.setCategory(category);
        }

        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        Long userId = userService.getCurrentUserId();
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
        expenseRepository.delete(expense);
    }


}
