package com.expense.controller;

import com.expense.model.Expense;
import com.expense.model.ExpenseDTO;
import java.util.stream.Collectors;


import com.expense.service.CategoryService;
import com.expense.service.ExpenseService;
import com.expense.expensetracker.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserService userService;





    // Category Endpoints
    @GetMapping("/categories")
    public List<String> getAllCategories() {
        return categoryService.getCategoryNames();
    }

    @PostMapping("/categories")
    public void addCategory(@RequestBody String category) {
        String cleanedCategory = category.replace("\"", "");
        categoryService.addCategory(cleanedCategory);
    }

    @DeleteMapping("/categories/{categoryName}")
    public void deleteCategory(@PathVariable String categoryName) {
        categoryService.deleteCategory(categoryName);
    }



    // Expense Endpoints
    @GetMapping("/expenses")
    public List<ExpenseDTO> getAllExpenses(@RequestParam(required = false) String category,
                                         @RequestParam(required = false) String period,
                                         @RequestParam(required = false) String startDate,
                                         @RequestParam(required = false) String endDate,
                                         @RequestParam(defaultValue = "desc") String sortOrder) {
        List<Expense> expenses = expenseService.getAllExpenses(category, period, startDate, endDate, sortOrder);
        return expenses.stream().map(ExpenseDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/expenses/{id}")
    public ExpenseDTO getExpenseById(@PathVariable Long id) {
        Expense expense = expenseService.getExpenseById(id);
        return new ExpenseDTO(expense);
    }

    @PostMapping("/expenses")
    public Expense createExpense(@RequestBody Expense expense) {
        Long userId = userService.getCurrentUserId();
        expense.setUserId(userId);
        if (expense.getCategory() != null && expense.getCategory().getCategoryName() != null) {
            expense.setCategory(expense.getCategory());
        }
        return expenseService.createExpense(expense);
    }

    @PutMapping("/expenses/{id}")
    public Expense updateExpense(@PathVariable Long id, @RequestBody Expense expenseDetails) {
        Long userId = userService.getCurrentUserId();
        expenseDetails.setUserId(userId);
        return expenseService.updateExpense(id, expenseDetails);
    }

    @DeleteMapping("/expenses/{id}")
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }


}
