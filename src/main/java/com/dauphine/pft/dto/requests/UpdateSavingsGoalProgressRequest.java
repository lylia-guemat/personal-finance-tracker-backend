package com.dauphine.pft.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSavingsGoalProgressRequest {

    @NotNull(message = "Amount to add is required")
    @PositiveOrZero(message = "Amount to add must be strictly positive")
    private BigDecimal currentAmount;
}
