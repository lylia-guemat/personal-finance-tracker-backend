package com.dauphine.pft.services;

import com.dauphine.pft.models.Budget;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BudgetService {

    List<Budget> getAllByUser(UUID userId);

    Budget create(UUID userId, UUID categoryId, BigDecimal monthlyCap);

    Budget update(UUID id, UUID userId, BigDecimal monthlyCap);

    void delete(UUID id, UUID userId);

    BigDecimal getCurrentMonthSpent(UUID userId, UUID categoryId);
}
