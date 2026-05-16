package com.dauphine.pft.services;

import com.dauphine.pft.models.SavingsGoal;
import com.dauphine.pft.models.SavingsGoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SavingsGoalService {

    // UC-G04 — Tous les objectifs de l'utilisateur
    List<SavingsGoal> getAllByUser(UUID userId);

    // Filtres combinés
    List<SavingsGoal> filter(
            UUID userId,
            SavingsGoalStatus status,
            String keyword,
            BigDecimal minTarget,
            BigDecimal maxTarget,
            LocalDate deadlineBefore,
            LocalDate deadlineAfter
    );

    // UC-G05 — Un objectif par id
    SavingsGoal getByIdAndUser(UUID id, UUID userId);

    // UC-G01 — Créer un objectif
    SavingsGoal create(UUID userId, String name, String description,
                       BigDecimal targetAmount, BigDecimal currentAmount, LocalDate deadline);

    // UC-G02 — Modifier un objectif
    SavingsGoal update(UUID id, UUID userId, String name, String description,
                       BigDecimal targetAmount, BigDecimal currentAmount,
                       LocalDate deadline, SavingsGoalStatus status);

    // UC-G06 — Mettre à jour uniquement la progression
    SavingsGoal updateProgress(UUID id, UUID userId, BigDecimal currentAmount);

    // UC-G03 — Supprimer un objectif
    void delete(UUID id, UUID userId);

    // Marquer les objectifs expirés comme FAILED
    void markOverdueAsFailed(UUID userId);
}
