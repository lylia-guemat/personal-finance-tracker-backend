package com.dauphine.pft.mappers;

import com.dauphine.pft.dto.responses.BudgetUsageResponse;
import com.dauphine.pft.models.Budget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class BudgetMapper {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MAX_PERCENT = BigDecimal.valueOf(150);

    private final CategoryMapper categoryMapper;

    public BudgetUsageResponse toUsageResponse(Budget budget, BigDecimal spent) {
        BigDecimal safeSpent = spent == null ? BigDecimal.ZERO : spent;
        BigDecimal remaining = budget.getMonthlyCap().subtract(safeSpent);
        BigDecimal rawPercent = budget.getMonthlyCap().signum() > 0
                ? safeSpent.multiply(ONE_HUNDRED).divide(budget.getMonthlyCap(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal displayPercent = rawPercent.min(MAX_PERCENT);

        return new BudgetUsageResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getMonthlyCap(),
                budget.getPeriod(),
                budget.getCreatedAt(),
                categoryMapper.toResponse(budget.getCategory()),
                safeSpent,
                remaining,
                displayPercent,
                statusFromPercent(rawPercent)
        );
    }

    private String statusFromPercent(BigDecimal percent) {
        if (percent.compareTo(BigDecimal.valueOf(60)) < 0) {
            return "safe";
        }

        if (percent.compareTo(BigDecimal.valueOf(90)) < 0) {
            return "warning";
        }

        return "danger";
    }
}
