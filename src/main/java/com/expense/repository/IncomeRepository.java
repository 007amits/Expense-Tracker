package com.expense.repository;

import com.expense.model.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    long countByAccount(String account);
    List<Income> findByAccount(String account);
    List<Income> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Income> findByUserId(Long userId);
    Optional<Income> findByIdAndUserId(Long id, Long userId);
    List<Income> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<Income> findByUserIdAndAccount(Long userId, String account);
}
