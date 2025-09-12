package com.expense.repository;

import com.expense.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByUserId(Long userId);
    Optional<Account> findByAccountNameAndUserId(String accountName, Long userId);

    @Transactional
    void deleteByAccountNameAndUserId(String accountName, Long userId);
}
