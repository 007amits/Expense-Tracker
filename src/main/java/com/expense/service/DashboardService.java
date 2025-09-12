package com.expense.service;

import com.expense.model.DashboardSummaryDTO;
import com.expense.model.Expense;
import com.expense.model.ExpenseDTO;
import com.expense.model.Income;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseService expenseService;

    public DashboardSummaryDTO getDashboardSummary(String period, String sortOrder) {
        List<Income> incomes = incomeService.getAllIncomes(null, period, null, null, sortOrder);
        List<Expense> expenses = expenseService.getAllExpenses(null, period, null, null, sortOrder);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double netTotal = totalIncome.subtract(totalExpense).doubleValue();

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalIncome(totalIncome.doubleValue());
        summary.setTotalExpense(totalExpense.doubleValue());
        summary.setNetTotal(netTotal);
        summary.setRecentIncomes(incomes);
        summary.setRecentExpenses(expenses.stream().map(ExpenseDTO::new).collect(Collectors.toList()));

        return summary;
    }
}
