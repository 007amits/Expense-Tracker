package com.expense.service;

import com.expense.model.Account;
import com.expense.expensetracker.service.UserService;
import com.expense.repository.AccountRepository;
import com.expense.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final IncomeRepository incomeRepository;
    private final UserService userService;

    // Placeholder for a real currency conversion service
    private double getExchangeRate(String fromCurrency, String toCurrency) {
        // In a real application, this would call an external API
        // For demonstration, we'll use a simple hardcoded rate
        if ("USD".equals(fromCurrency) && "INR".equals(toCurrency)) {
            return 83.0;
        }
        if ("INR".equals(fromCurrency) && "USD".equals(toCurrency)) {
            return 1 / 83.0;
        }
        return 1.0; // Default to 1:1 if no rate is found
    }

    private final AccountRepository accountRepository;


    @Autowired
    public AccountService(AccountRepository accountRepository, IncomeRepository incomeRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.incomeRepository = incomeRepository;
        this.userService = userService;
    }

    public List<Account> getAccounts(Long userId) {
        return accountRepository.findAllByUserId(userId);
    }
    
    /**
     * Get all accounts for the current user
     * @return List of accounts belonging to the current user
     */
    public List<Account> getAllAccounts() {
        Long userId = userService.getCurrentUserId();
        return getAccounts(userId);
    }

    public List<String> getAccountNames(Long userId) {
        return getAccounts(userId).stream()
                .map(Account::getAccountName)
                .collect(Collectors.toList());
    }

    public Account addAccount(String accountName, Long userId) {
        // First, check if the account name is empty or null
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty");
        }
        
        // Normalize the account name (trim whitespace)
        String normalizedAccountName = accountName.trim();
        
        // Check if the account already exists for this user
        Optional<Account> existingAccount = accountRepository.findByAccountNameAndUserId(normalizedAccountName, userId);
        if (existingAccount.isPresent()) {
            // Account with this name already exists for this user
            throw new IllegalArgumentException("You already have an account named '" + normalizedAccountName + "'. Please choose a different name.");
        }
        
        try {
            // Create new account with the given name for this user
            Account newAccount = new Account(normalizedAccountName, BigDecimal.ZERO, "INR");
            newAccount.setUserId(userId);
            return accountRepository.save(newAccount);
        } catch (Exception e) {
            // Better error handling for constraint violations
            if (e.getMessage() != null && 
                (e.getMessage().contains("UNIQUE constraint failed") || 
                e.getMessage().contains("unique constraint"))) {
                // This should only happen if there's a database constraint issue
                // Since we've already checked for duplicate account names for this user
                throw new IllegalArgumentException("Unable to create account due to a database constraint. Please try a different account name.");
            }
            throw e;
        }
    }

    public void addIncome(String accountName, BigDecimal amount, String incomeCurrency, Long userId) {
        Optional<Account> existingAccount = accountRepository.findByAccountNameAndUserId(accountName, userId);
        if (existingAccount.isPresent()) {
            Account account = existingAccount.get();
            BigDecimal convertedAmount = amount;
            if (incomeCurrency != null && !incomeCurrency.equalsIgnoreCase(account.getCurrencyCode())) {
                double exchangeRate = getExchangeRate(incomeCurrency, account.getCurrencyCode());
                convertedAmount = amount.multiply(BigDecimal.valueOf(exchangeRate));
            }
            account.setAccountBalance(account.getAccountBalance().add(convertedAmount));
            accountRepository.save(account);
        } else {
            String newAccountCurrency = (incomeCurrency != null && !incomeCurrency.isEmpty()) ? incomeCurrency : "INR";
            Account newAccount = new Account(accountName, amount, newAccountCurrency);
            newAccount.setUserId(userId);
            accountRepository.save(newAccount);
        }
    }

    public void subtractIncome(String accountName, BigDecimal amount, String incomeCurrency, Long userId) {
        Optional<Account> existingAccount = accountRepository.findByAccountNameAndUserId(accountName, userId);
        if (existingAccount.isPresent()) {
            Account account = existingAccount.get();
            BigDecimal convertedAmount = amount;
            if (incomeCurrency != null && !incomeCurrency.equalsIgnoreCase(account.getCurrencyCode())) {
                double exchangeRate = getExchangeRate(incomeCurrency, account.getCurrencyCode());
                convertedAmount = amount.multiply(BigDecimal.valueOf(exchangeRate));
            }
            account.setAccountBalance(account.getAccountBalance().subtract(convertedAmount));
            accountRepository.save(account);
        }
    }



    public void deleteAccount(String accountName, Long userId) {


        long incomeCount = incomeRepository.countByAccount(accountName);
        if (incomeCount > 0) {
            throw new IllegalStateException("Cannot delete account with associated income records.");
        }

        accountRepository.deleteByAccountNameAndUserId(accountName, userId);
    }
}
