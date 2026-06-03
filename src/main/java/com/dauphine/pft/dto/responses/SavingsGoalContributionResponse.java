package com.dauphine.pft.dto.responses;

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
public class SavingsGoalContributionResponse {

    private UUID id;
    private UUID goalId;
    private BigDecimal amount;
    private LocalDate date;
    private String note;
    private LocalDateTime createdAt;
}
