package com.dauphine.pft.mappers;

import com.dauphine.pft.dto.responses.SavingsGoalContributionResponse;
import com.dauphine.pft.models.SavingsGoalContribution;
import org.springframework.stereotype.Component;

@Component
public class SavingsGoalContributionMapper {

    public SavingsGoalContributionResponse toResponse(SavingsGoalContribution contribution) {
        if (contribution == null) return null;

        return new SavingsGoalContributionResponse(
                contribution.getId(),
                contribution.getGoal().getId(),
                contribution.getAmount(),
                contribution.getDate(),
                contribution.getNote(),
                contribution.getCreatedAt()
        );
    }
}
