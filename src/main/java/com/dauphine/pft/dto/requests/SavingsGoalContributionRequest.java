package com.dauphine.pft.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalContributionRequest {

    @NotNull(message = "Contribution amount is required")
    @Positive(message = "Contribution amount must be strictly positive")
    private BigDecimal amount;

    @NotNull(message = "Contribution date is required")
    private LocalDate date;

    @Size(max = 500, message = "Contribution note cannot exceed 500 characters")
    private String note;
}
