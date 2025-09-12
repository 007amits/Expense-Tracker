package com.expense.controller;

import com.expense.expensetracker.service.UserService;
import com.expense.model.Expense;
import com.expense.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public String showAnalytics(Model model) {
        // Add current user ID to model for security filtering
        model.addAttribute("userId", userService.getCurrentUserId());
        return "analytics";
    }
    
    @GetMapping("/category-data")
    @ResponseBody
    public Map<String, Object> getCategoryData(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        // Get expenses filtered by period
        List<Expense> expenses = expenseService.getAllExpenses(null, period, startDate, endDate, "desc");
        
        // Group expenses by category and sum amounts
        Map<String, BigDecimal> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory().getCategoryName(), // Get category name as String
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
        
        // Prepare data for pie chart
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categoryTotals.keySet());
        result.put("amounts", categoryTotals.values());
        
        return result;
    }
    
    @GetMapping("/monthly-data")
    @ResponseBody
    public Map<String, Object> getMonthlyData(
            @RequestParam(required = false) String year) {
        
        // Default to current year if not specified
        int targetYear = year != null ? Integer.parseInt(year) : LocalDate.now().getYear();
        
        // Get all expenses for the user
        List<Expense> expenses = expenseService.getAllExpenses(null, "all", null, null, "asc");
        
        // Filter expenses for the target year
        expenses = expenses.stream()
                .filter(expense -> expense.getDate().getYear() == targetYear)
                .collect(Collectors.toList());
        
        // Group expenses by month and sum amounts
        Map<Integer, BigDecimal> monthlyTotals = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            final int month = i;
            BigDecimal total = expenses.stream()
                    .filter(expense -> expense.getDate().getMonthValue() == month)
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyTotals.put(month, total);
        }
        
        // Prepare data for line chart
        Map<String, Object> result = new HashMap<>();
        result.put("months", monthlyTotals.keySet());
        result.put("amounts", monthlyTotals.values());
        result.put("year", targetYear);
        
        return result;
    }
}
