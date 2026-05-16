package com.dauphine.pft.repositories;

import com.dauphine.pft.models.SavingsGoal;
import com.dauphine.pft.models.SavingsGoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // Filtre combiné
    @Query(value = """
    SELECT sg.* FROM savings_goal sg
    WHERE sg.user_id = :userId
    AND (CAST(:status AS VARCHAR) IS NULL OR sg.status = CAST(:status AS VARCHAR))
    AND (:keyword IS NULL OR LOWER(sg.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(sg.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (CAST(:minTarget AS NUMERIC) IS NULL OR sg.target_amount >= CAST(:minTarget AS NUMERIC))
    AND (CAST(:maxTarget AS NUMERIC) IS NULL OR sg.target_amount <= CAST(:maxTarget AS NUMERIC))
    AND (CAST(:deadlineBefore AS DATE) IS NULL OR sg.deadline <= CAST(:deadlineBefore AS DATE))
    AND (CAST(:deadlineAfter AS DATE) IS NULL OR sg.deadline >= CAST(:deadlineAfter AS DATE))
    ORDER BY sg.deadline ASC NULLS LAST
    """, nativeQuery = true)
    List<SavingsGoal> findWithFilters(
            @Param("userId") UUID userId,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("minTarget") BigDecimal minTarget,
            @Param("maxTarget") BigDecimal maxTarget,
            @Param("deadlineBefore") LocalDate deadlineBefore,
            @Param("deadlineAfter") LocalDate deadlineAfter
    );

    // Trouver tous les objectifs IN_PROGRESS avec deadline dépassée
    @Query("""
        SELECT g FROM SavingsGoal g
        WHERE g.user.id = :userId
        AND g.status = 'IN_PROGRESS'
        AND g.deadline < :today
    """)
    List<SavingsGoal> findOverdueGoals(
            @Param("userId") UUID userId,
            @Param("today") LocalDate today
    );

    // UC-D06 : total target et current pour un utilisateur
    @Query("""
    SELECT COALESCE(SUM(g.targetAmount), 0)
    FROM SavingsGoal g
    WHERE g.user.id = :userId
    """)
    BigDecimal sumTargetAmountByUserId(@Param("userId") UUID userId);

    @Query("""
    SELECT COALESCE(SUM(g.currentAmount), 0)
    FROM SavingsGoal g
    WHERE g.user.id = :userId
    """)
    BigDecimal sumCurrentAmountByUserId(@Param("userId") UUID userId);

    // Compter par statut
    long countByUserIdAndStatus(UUID userId, SavingsGoalStatus status);

    // Compter total
    long countByUserId(UUID userId);
}
