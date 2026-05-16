package com.dauphine.pft.mappers;

import com.dauphine.pft.dto.responses.SavingsGoalResponse;
import com.dauphine.pft.models.SavingsGoal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SavingsGoalMapper {

    // Entity → Response DTO
    public SavingsGoalResponse toResponse(SavingsGoal goal) {
        if (goal == null) return null;

        // Calcul du pourcentage de progression
        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (goal.getTargetAmount() != null
                && goal.getCurrentAmount() != null
                && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = goal.getCurrentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        }

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getDescription(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                progressPercentage,
                goal.getDeadline(),
                goal.getStatus(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
