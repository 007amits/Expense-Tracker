package com.expense.model;

import java.util.List;

public class DashboardSummaryDTO {
    private double totalIncome;
    private double totalExpense;
    private double netTotal;
    private List<Income> recentIncomes;
    private List<ExpenseDTO> recentExpenses;

    // Getters and Setters
    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public List<Income> getRecentIncomes() {
        return recentIncomes;
    }

    public void setRecentIncomes(List<Income> recentIncomes) {
        this.recentIncomes = recentIncomes;
    }

    public List<ExpenseDTO> getRecentExpenses() {
        return recentExpenses;
    }

    public void setRecentExpenses(List<ExpenseDTO> recentExpenses) {
        this.recentExpenses = recentExpenses;
    }
}
