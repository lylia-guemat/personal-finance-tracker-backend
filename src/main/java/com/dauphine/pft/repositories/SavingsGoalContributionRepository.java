package com.dauphine.pft.repositories;

import com.dauphine.pft.models.SavingsGoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavingsGoalContributionRepository extends JpaRepository<SavingsGoalContribution, UUID> {

    List<SavingsGoalContribution> findByGoalIdAndGoalUserIdOrderByDateDescCreatedAtDesc(UUID goalId, UUID userId);
}
