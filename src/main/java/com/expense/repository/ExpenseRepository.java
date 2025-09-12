package com.expense.repository;

import com.expense.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category WHERE e.userId = :userId")
    List<Expense> findByUserId(Long userId);

    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category WHERE e.userId = :userId")
    List<Expense> findAllByUserId(Long userId);

    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate")
    List<Expense> findAllByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    long countByPaymentMode(String paymentMode);

    List<Expense> findByPaymentMode(String paymentMode);

    long countByCategoryCategoryNameAndUserId(String categoryName, Long userId);
    
    Optional<Expense> findByIdAndUserId(Long id, Long userId);
}
