package com.expense.service;

import com.expense.expensetracker.service.UserService;
import com.expense.model.Income;
import com.expense.repository.IncomeRepository;
import com.expense.util.DateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

@Service
public class IncomeService {
    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    public List<Income> getAllIncomes(String account, String period, String startDate, String endDate, String sortOrder) {
        Long userId = userService.getCurrentUserId();
        List<Income> incomes = incomeRepository.findByUserId(userId);

        if (StringUtils.hasText(account)) {
            incomes = incomes.stream()
                    .filter(income -> account.equalsIgnoreCase(income.getAccount()))
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(period) && !"all".equalsIgnoreCase(period)) {
            LocalDate start, end;
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
            LocalDate finalStart = start;
            LocalDate finalEnd = end;
            incomes = incomes.stream()
                    .filter(income -> !income.getDate().isBefore(finalStart) && !income.getDate().isAfter(finalEnd))
                    .collect(Collectors.toList());
        }

        if ("asc".equalsIgnoreCase(sortOrder)) {
            incomes.sort((i1, i2) -> i1.getDate().compareTo(i2.getDate()));
        } else {
            incomes.sort((i1, i2) -> i2.getDate().compareTo(i1.getDate()));
        }
        return incomes;
    }

    public Income getIncomeById(Long id) {
        Long userId = userService.getCurrentUserId();
        return incomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));
    }

    public Income createIncome(Income income) {
        // Validate that the date is not in the future
        DateValidator.validateDateNotInFuture(income.getDate());
        
        Long userId = userService.getCurrentUserId();

        if (income.getAccount() == null || income.getAccount().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty.");
        }
        
        try {
            accountService.addIncome(income.getAccount(), income.getAmount(), income.getCurrency(), userId);
            income.setUserId(userId);
            Income savedIncome = incomeRepository.save(income);
            return savedIncome;
        } catch (Exception e) {
            throw e; // Re-throw the exception to ensure the transaction is rolled back
        }
    }

    /**
     * Get the total amount of all income for the current user
     * @return Total income as BigDecimal
     */
    public BigDecimal getTotalIncome() {
        Long userId = userService.getCurrentUserId();
        List<Income> incomes = incomeRepository.findByUserId(userId);
        
        return incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Income updateIncome(Long id, Income incomeDetails) {
        // Validate that the date is not in the future
        DateValidator.validateDateNotInFuture(incomeDetails.getDate());
        
        Long userId = userService.getCurrentUserId();
        Income existingIncome = incomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));
        BigDecimal oldAmount = existingIncome.getAmount();
        BigDecimal newAmount = incomeDetails.getAmount();

        // Calculate the difference to update the account balance
        BigDecimal difference = newAmount.subtract(oldAmount);

        // Update account balance with the difference
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            accountService.addIncome(existingIncome.getAccount(), difference, existingIncome.getCurrency(), userId);
        }

        // Update income details
        existingIncome.setAmount(newAmount);
        existingIncome.setDate(incomeDetails.getDate());
        existingIncome.setNote(incomeDetails.getNote());
        existingIncome.setCurrency(incomeDetails.getCurrency());

        return incomeRepository.save(existingIncome);
    }

    public void deleteIncome(Long id) {
        Long userId = userService.getCurrentUserId();
        Income income = incomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));

        // Subtract the income amount from the associated account
        accountService.subtractIncome(income.getAccount(), income.getAmount(), income.getCurrency(), userId);

        incomeRepository.deleteById(id);
    }


}
