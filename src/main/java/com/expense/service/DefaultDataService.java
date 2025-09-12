package com.expense.service;

import com.expense.model.Account;
import com.expense.model.Category;
import com.expense.repository.AccountRepository;
import com.expense.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Service to create default data for new users
 */
@Service
public class DefaultDataService {

    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public DefaultDataService(CategoryRepository categoryRepository, AccountRepository accountRepository) {
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Create default categories for a new user
     * @param userId The ID of the newly created user
     */
    public void createDefaultCategories(Long userId) {
        List<String> defaultCategories = Arrays.asList("Grocery", "Food", "Travel", "Daily Needs");
        
        for (String categoryName : defaultCategories) {
            Category category = new Category(categoryName, userId);
            categoryRepository.save(category);
        }
    }

    /**
     * Create default accounts for a new user
     * @param userId The ID of the newly created user
     */
    public void createDefaultAccounts(Long userId) {
        List<String> defaultAccounts = Arrays.asList("Salary", "Investment");
        
        for (String accountName : defaultAccounts) {
            Account account = new Account(accountName, BigDecimal.ZERO, "INR");
            account.setUserId(userId);
            accountRepository.save(account);
        }
    }

    /**
     * Initialize all default data for a new user
     * @param userId The ID of the newly created user
     */
    public void initializeUserData(Long userId) {
        createDefaultCategories(userId);
        createDefaultAccounts(userId);
    }
}
