package com.dauphine.pft.repositories;

import com.dauphine.pft.models.Budget;
import com.dauphine.pft.models.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserId(UUID userId);

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    Optional<Budget> findByUserIdAndCategoryIdAndPeriod(
            UUID userId,
            UUID categoryId,
            BudgetPeriod period
    );

    boolean existsByUserIdAndCategoryIdAndPeriod(
            UUID userId,
            UUID categoryId,
            BudgetPeriod period
    );
}
