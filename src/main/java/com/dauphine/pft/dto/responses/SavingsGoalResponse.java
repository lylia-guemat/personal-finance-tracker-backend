package com.dauphine.pft.dto.responses;

import com.dauphine.pft.models.SavingsGoalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal progressPercentage;  // calculé côté service
    private LocalDate deadline;
    private SavingsGoalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
