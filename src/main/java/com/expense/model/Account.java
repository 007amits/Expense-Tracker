package com.expense.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "account_balance", nullable = false)
    private java.math.BigDecimal accountBalance;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public Account() {
    }

    public Account(String accountName, java.math.BigDecimal accountBalance, String currencyCode) {
        this.accountName = accountName;
        this.accountBalance = accountBalance;
        this.currencyCode = currencyCode;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public java.math.BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(java.math.BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
