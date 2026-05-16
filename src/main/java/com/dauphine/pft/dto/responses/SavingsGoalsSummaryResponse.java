package com.dauphine.pft.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalsSummaryResponse {

    private long totalGoals;
    private long completedGoals;
    private long failedGoals;
    private long cancelledGoals;
    private long inProgressGoals;
    private BigDecimal totalTargetAmount;
    private BigDecimal totalCurrentAmount;
    private BigDecimal globalProgressPercentage;
}
