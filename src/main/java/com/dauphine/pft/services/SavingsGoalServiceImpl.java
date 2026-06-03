package com.dauphine.pft.services;

import com.dauphine.pft.exceptions.*;
import com.dauphine.pft.models.AppUser;
import com.dauphine.pft.models.SavingsGoal;
import com.dauphine.pft.models.SavingsGoalContribution;
import com.dauphine.pft.models.SavingsGoalStatus;
import com.dauphine.pft.repositories.AppUserRepository;
import com.dauphine.pft.repositories.SavingsGoalContributionRepository;
import com.dauphine.pft.repositories.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsGoalContributionRepository savingsGoalContributionRepository;
    private final AppUserRepository appUserRepository;

    // UC-G04
    @Override
    public List<SavingsGoal> getAllByUser(UUID userId) {
        return savingsGoalRepository.findByUserId(userId);
    }

    // Filtres combinés
    @Override
    public List<SavingsGoal> filter(UUID userId, SavingsGoalStatus status, String keyword,
                                    BigDecimal minTarget, BigDecimal maxTarget,
                                    LocalDate deadlineBefore, LocalDate deadlineAfter) {

        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword : null;
        // Convertir l'enum en String pour la requête native
        String statusStr = status != null ? status.name() : null;

        boolean hasFilters = status != null || cleanKeyword != null
                || minTarget != null || maxTarget != null
                || deadlineBefore != null || deadlineAfter != null;

        if (!hasFilters) {
            return savingsGoalRepository.findByUserId(userId);
        }

        return savingsGoalRepository.findWithFilters(
                userId, statusStr, cleanKeyword,
                minTarget, maxTarget, deadlineBefore, deadlineAfter);
    }

    // UC-G05
    @Override
    public SavingsGoal getByIdAndUser(UUID id, UUID userId) {
        return savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SavingsGoalNotFoundException(id));
    }

    // UC-G01
    @Override
    public SavingsGoal create(UUID userId, String name, String description,
                              BigDecimal targetAmount, BigDecimal currentAmount,
                              LocalDate deadline) {

        // Valeur par défaut si currentAmount non fourni
        if (currentAmount == null) {
            currentAmount = BigDecimal.ZERO;
        }

        // UC-G07 : vérifier que currentAmount ne dépasse pas targetAmount
        if (currentAmount.compareTo(targetAmount) > 0) {
            throw new InvalidSavingsGoalAmountException();
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SavingsGoal goal = new SavingsGoal();
        goal.setName(name);
        goal.setDescription(description);
        goal.setTargetAmount(targetAmount);
        goal.setCurrentAmount(currentAmount);
        goal.setDeadline(deadline);
        goal.setUser(user);

        // UC-G07 : statut automatique à la création
        if (currentAmount.compareTo(targetAmount) == 0) {
            goal.setStatus(SavingsGoalStatus.COMPLETED);
        } else {
            goal.setStatus(SavingsGoalStatus.IN_PROGRESS);
        }

        return savingsGoalRepository.save(goal);
    }

    // UC-G02
    @Override
    public SavingsGoal update(UUID id, UUID userId, String name, String description,
                              BigDecimal targetAmount, BigDecimal currentAmount,
                              LocalDate deadline, SavingsGoalStatus status) {

        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SavingsGoalNotFoundException(id));

        // UC : interdire la modification d'un objectif déjà COMPLETED
        if (goal.getStatus() == SavingsGoalStatus.COMPLETED) {
            throw new SavingsGoalAlreadyCompletedException(id);
        }

        // UC : vérifier que currentAmount ne dépasse pas targetAmount
        if (currentAmount.compareTo(targetAmount) > 0) {
            throw new InvalidSavingsGoalAmountException();
        }

        goal.setName(name);
        goal.setDescription(description);
        goal.setTargetAmount(targetAmount);
        goal.setCurrentAmount(currentAmount);
        goal.setDeadline(deadline);

        // UC-G07 : statut automatique si objectif atteint, sinon respecter le choix
        if (currentAmount.compareTo(targetAmount) >= 0) {
            goal.setStatus(SavingsGoalStatus.COMPLETED);
        } else {
            goal.setStatus(status); // IN_PROGRESS ou CANCELLED selon choix utilisateur
        }

        return savingsGoalRepository.save(goal);
    }

    // UC-G06
    @Override
    public SavingsGoal updateProgress(UUID id, UUID userId, BigDecimal amountToAdd) {

        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SavingsGoalNotFoundException(id));

        if (goal.getStatus() == SavingsGoalStatus.COMPLETED) {
            throw new SavingsGoalAlreadyCompletedException(id);
        }

        // Vérifier que le montant à ajouter est positif
        if (amountToAdd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSavingsGoalAmountException();
        }

        // Additionner au lieu d'écraser
        BigDecimal newAmount = goal.getCurrentAmount().add(amountToAdd);

        // Vérifier que le nouveau total ne dépasse pas targetAmount
        if (newAmount.compareTo(goal.getTargetAmount()) > 0) {
            throw new InvalidSavingsGoalAmountException();
        }

        goal.setCurrentAmount(newAmount);

        // Marquer automatiquement COMPLETED si objectif atteint
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoalStatus.COMPLETED);
        }

        return savingsGoalRepository.save(goal);
    }

    @Override
    public List<SavingsGoalContribution> getContributions(UUID goalId, UUID userId) {
        getByIdAndUser(goalId, userId);
        return savingsGoalContributionRepository.findByGoalIdAndGoalUserIdOrderByDateDescCreatedAtDesc(goalId, userId);
    }

    @Override
    @Transactional
    public SavingsGoalContribution contribute(UUID goalId, UUID userId, BigDecimal amount, LocalDate date, String note) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new SavingsGoalNotFoundException(goalId));

        if (goal.getStatus() == SavingsGoalStatus.COMPLETED) {
            throw new SavingsGoalAlreadyCompletedException(goalId);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSavingsGoalAmountException();
        }

        BigDecimal newAmount = goal.getCurrentAmount().add(amount);

        if (newAmount.compareTo(goal.getTargetAmount()) > 0) {
            throw new InvalidSavingsGoalAmountException();
        }

        SavingsGoalContribution contribution = new SavingsGoalContribution();
        contribution.setGoal(goal);
        contribution.setAmount(amount);
        contribution.setDate(date);
        contribution.setNote(note);

        goal.setCurrentAmount(newAmount);
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoalStatus.COMPLETED);
        }

        savingsGoalRepository.save(goal);
        return savingsGoalContributionRepository.save(contribution);
    }

    // UC-G03
    @Override
    public void delete(UUID id, UUID userId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SavingsGoalNotFoundException(id));
        savingsGoalRepository.delete(goal);
    }

    // Marquer automatiquement les objectifs expirés comme FAILED
    @Override
    public void markOverdueAsFailed(UUID userId) {
        List<SavingsGoal> overdueGoals = savingsGoalRepository
                .findOverdueGoals(userId, LocalDate.now());

        overdueGoals.forEach(goal -> {
            goal.setStatus(SavingsGoalStatus.FAILED);
            savingsGoalRepository.save(goal);
        });
    }
}
