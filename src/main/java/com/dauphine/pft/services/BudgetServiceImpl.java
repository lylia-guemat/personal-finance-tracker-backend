package com.dauphine.pft.services;

import com.dauphine.pft.exceptions.BudgetAlreadyExistsException;
import com.dauphine.pft.exceptions.BudgetCategoryTypeMismatchException;
import com.dauphine.pft.exceptions.BudgetNotFoundException;
import com.dauphine.pft.exceptions.CategoryNotFoundException;
import com.dauphine.pft.models.AppUser;
import com.dauphine.pft.models.Budget;
import com.dauphine.pft.models.BudgetPeriod;
import com.dauphine.pft.models.Category;
import com.dauphine.pft.models.CategoryType;
import com.dauphine.pft.repositories.AppUserRepository;
import com.dauphine.pft.repositories.BudgetRepository;
import com.dauphine.pft.repositories.CategoryRepository;
import com.dauphine.pft.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository appUserRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<Budget> getAllByUser(UUID userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Override
    public Budget create(UUID userId, UUID categoryId, BigDecimal monthlyCap) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        validateExpenseCategory(category);

        return budgetRepository.findByUserIdAndCategoryIdAndPeriod(userId, categoryId, BudgetPeriod.MONTHLY)
                .map(existing -> update(existing.getId(), userId, monthlyCap))
                .orElseGet(() -> createNewBudget(userId, category, monthlyCap));
    }

    @Override
    public Budget update(UUID id, UUID userId, BigDecimal monthlyCap) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BudgetNotFoundException(id));
        budget.setMonthlyCap(monthlyCap);
        return budgetRepository.save(budget);
    }

    @Override
    public void delete(UUID id, UUID userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BudgetNotFoundException(id));
        budgetRepository.delete(budget);
    }

    @Override
    public BigDecimal getCurrentMonthSpent(UUID userId, UUID categoryId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
        BigDecimal spent = transactionRepository.sumExpensesByUserAndCategoryBetween(userId, categoryId, start, end);
        return spent == null ? BigDecimal.ZERO : spent;
    }

    private Budget createNewBudget(UUID userId, Category category, BigDecimal monthlyCap) {
        if (budgetRepository.existsByUserIdAndCategoryIdAndPeriod(
                userId,
                category.getId(),
                BudgetPeriod.MONTHLY
        )) {
            throw new BudgetAlreadyExistsException(category.getId());
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setMonthlyCap(monthlyCap);
        budget.setPeriod(BudgetPeriod.MONTHLY);

        return budgetRepository.save(budget);
    }

    private void validateExpenseCategory(Category category) {
        if (category.getType() == CategoryType.INCOME) {
            throw new BudgetCategoryTypeMismatchException();
        }
    }
}
