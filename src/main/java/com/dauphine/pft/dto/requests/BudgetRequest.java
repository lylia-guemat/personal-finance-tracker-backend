package com.dauphine.pft.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @NotNull(message = "Monthly cap is required")
    @Positive(message = "Monthly cap must be positive")
    private BigDecimal monthlyCap;
}
