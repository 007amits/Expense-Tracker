package com.expense.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String categoryName;
    private String paymentMode;
    private String note;
    private String currency;
    private Long userId;

    public ExpenseDTO(Expense expense) {
        this.id = expense.getId();
        this.description = expense.getDescription();
        this.amount = expense.getAmount();
        this.date = expense.getDate();
        this.categoryName = (expense.getCategory() != null) ? expense.getCategory().getCategoryName() : "N/A";
        this.paymentMode = expense.getPaymentMode();
        this.note = expense.getNote();
        this.currency = expense.getCurrency();
        this.userId = expense.getUserId();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
