package com.dauphine.pft.repositories;

import com.dauphine.pft.models.SavingsGoal;
import com.dauphine.pft.models.SavingsGoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {

    // Tous les objectifs d'un utilisateur
    List<SavingsGoal> findByUserId(UUID userId);

    // Récupérer un objectif par id et userId
    Optional<SavingsGoal> findByIdAndUserId(UUID id, UUID userId);

    // Objectifs par statut pour un utilisateur
    List<SavingsGoal> findByUserIdAndStatus(UUID userId, SavingsGoalStatus status);
}
