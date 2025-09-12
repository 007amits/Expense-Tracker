package com.expense.event;

import org.springframework.context.ApplicationEvent;

public class AccountDeletionEvent extends ApplicationEvent {
    private final String accountName;

    public AccountDeletionEvent(Object source, String accountName) {
        super(source);
        this.accountName = accountName;
    }

    public String getAccountName() {
        return accountName;
    }
}
