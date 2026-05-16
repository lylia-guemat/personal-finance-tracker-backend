package com.dauphine.pft.dto.requests;

import com.dauphine.pft.models.SavingsGoalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class SavingsGoalRequest {

    @NotBlank(message = "Goal name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    private String description;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be strictly positive")
    private BigDecimal targetAmount;

    @NotNull(message = "Current amount is required")
    @PositiveOrZero(message = "Current amount must be zero or positive")
    private BigDecimal currentAmount;

    private LocalDate deadline;

    // À la création le statut est calculé automatiquement
    private SavingsGoalStatus status;
}

