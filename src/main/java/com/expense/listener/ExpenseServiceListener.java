package com.expense.listener;

import com.expense.event.AccountDeletionEvent;
import com.expense.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;



public class ExpenseServiceListener implements ApplicationListener<AccountDeletionEvent> {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseServiceListener(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public void onApplicationEvent(@NonNull AccountDeletionEvent event) {
        long expenseCount = expenseRepository.countByPaymentMode(event.getAccountName());
        if (expenseCount > 0) {
            throw new IllegalStateException("Cannot delete account with associated expense records.");
        }
    }
}
