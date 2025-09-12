package com.expense.controller;

import com.expense.expensetracker.service.UserService;
import com.expense.model.Expense;
import com.expense.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/expense-view")
public class ExpenseViewController {

    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public String showExpenses(Model model) {
        // Add current user ID to model for security filtering
        model.addAttribute("userId", userService.getCurrentUserId());
        return "expenses";
    }
    
    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> getExpensesData(
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        // Get expenses filtered by parameters
        List<Expense> expenses = expenseService.getAllExpenses(category, period, startDate, endDate, sortOrder);
        
        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("expenses", expenses);
        
        return response;
    }
}
