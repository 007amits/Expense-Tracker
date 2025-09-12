package com.expense.controller;

import com.expense.expensetracker.model.User;
import com.expense.expensetracker.service.UserService;
import com.expense.model.Account;
import com.expense.service.AccountService;
import com.expense.service.ExpenseService;
import com.expense.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private IncomeService incomeService;
    
    @GetMapping("/profile")
    public String showProfile(Model model) {
        // Get current user
        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        // Add user to model
        model.addAttribute("user", user);
        
        // Get account summary
        Map<String, Object> accountSummary = getAccountSummary(userId);
        model.addAttribute("accountSummary", accountSummary);
        
        return "profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User userDetails, RedirectAttributes redirectAttributes) {
        try {
            // Validate date of birth is not in the future
            if (userDetails.getDateOfBirth() != null && userDetails.getDateOfBirth().isAfter(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Date of birth cannot be in the future.");
                return "redirect:/profile";
            }
            
            // Get current user
            Long userId = userService.getCurrentUserId();
            User currentUser = userService.findById(userId);
            
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Update user details
            currentUser.setFirstName(userDetails.getFirstName());
            currentUser.setLastName(userDetails.getLastName());
            currentUser.setEmail(userDetails.getEmail());
            currentUser.setPhoneNumber(userDetails.getPhoneNumber());
            currentUser.setDateOfBirth(userDetails.getDateOfBirth());
            currentUser.setGender(userDetails.getGender());
            currentUser.setOccupation(userDetails.getOccupation());
            
            // Save updated user
            userService.update(currentUser);
            
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    private Map<String, Object> getAccountSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // Get all accounts for the user
        List<Account> accounts = accountService.getAllAccounts();
        
        // Calculate total accounts
        int totalAccounts = accounts.size();
        
        // Get total expenses
        BigDecimal totalExpenses = expenseService.getTotalExpenses();
        
        // Get total income
        BigDecimal totalIncome = incomeService.getTotalIncome();
        
        // Calculate total balance as Total Income - Total Expenses
        BigDecimal totalBalance = totalIncome.subtract(totalExpenses);
        
        // Add to summary
        summary.put("totalAccounts", totalAccounts);
        summary.put("totalBalance", totalBalance);
        summary.put("totalExpenses", totalExpenses);
        summary.put("totalIncome", totalIncome);
        summary.put("balanceStatus", getBalanceStatus(totalBalance));
        
        return summary;
    }
    
    /**
     * Determine the status of the balance for color coding
     * @param balance The balance amount
     * @return "positive", "negative", or "zero"
     */
    private String getBalanceStatus(BigDecimal balance) {
        int comparison = balance.compareTo(BigDecimal.ZERO);
        if (comparison > 0) {
            return "positive";
        } else if (comparison < 0) {
            return "negative";
        } else {
            return "zero";
        }
    }
}
