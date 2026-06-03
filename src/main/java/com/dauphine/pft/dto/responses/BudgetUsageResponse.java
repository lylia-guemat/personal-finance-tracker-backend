package com.dauphine.pft.dto.responses;

import com.dauphine.pft.models.BudgetPeriod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetUsageResponse {

    private UUID id;
    private UUID categoryId;
    private BigDecimal monthlyCap;
    private BudgetPeriod period;
    private LocalDateTime createdAt;
    private CategoryResponse category;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BigDecimal percent;
    private String status;
}
