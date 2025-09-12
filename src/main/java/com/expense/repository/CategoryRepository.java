package com.expense.repository;

import com.expense.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByUserId(Long userId);
    java.util.Optional<Category> findByCategoryNameAndUserId(String categoryName, Long userId);
}
