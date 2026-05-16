package com.dauphine.pft.services;

import com.dauphine.pft.dto.responses.*;
import com.dauphine.pft.models.SavingsGoalStatus;
import com.dauphine.pft.models.TransactionType;
import com.dauphine.pft.repositories.SavingsGoalRepository;
import com.dauphine.pft.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    // UC-D01
    @Override
    public DashboardSummaryResponse getSummary(UUID userId) {
        BigDecimal totalIncome = nullSafe(
                transactionRepository.sumByUserIdAndType(userId, TransactionType.INCOME));
        BigDecimal totalExpense = nullSafe(
                transactionRepository.sumByUserIdAndType(userId, TransactionType.EXPENSE));
        BigDecimal balance = totalIncome.subtract(totalExpense);
        long count = transactionRepository.countByUserId(userId);

        // Taux d'épargne = (income - expense) / income * 100
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = balance
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalIncome, 2, RoundingMode.HALF_UP);
        }

        return new DashboardSummaryResponse(
                totalIncome, totalExpense, balance, count, savingsRate);
    }

    // UC-D02
    @Override
    public MonthlySummaryResponse getMonthlySummary(UUID userId, int year, int month) {
        BigDecimal totalIncome = nullSafe(
                transactionRepository.sumByUserIdAndTypeAndMonth(
                        userId, TransactionType.INCOME, year, month));
        BigDecimal totalExpense = nullSafe(
                transactionRepository.sumByUserIdAndTypeAndMonth(
                        userId, TransactionType.EXPENSE, year, month));
        BigDecimal balance = totalIncome.subtract(totalExpense);
        long count = transactionRepository.countByUserIdAndMonth(userId, year, month);

        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = balance
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalIncome, 2, RoundingMode.HALF_UP);
        }

        return new MonthlySummaryResponse(
                year, month, totalIncome, totalExpense, balance, count, savingsRate);
    }

    // UC-D03
    @Override
    public List<CategoryAmountResponse> getExpensesByCategory(
            UUID userId, LocalDate startDate, LocalDate endDate) {
        return buildEnrichedCategoryList(
                transactionRepository.sumByCategoryAndTypeEnriched(
                        userId, TransactionType.EXPENSE.name(), startDate, endDate));
    }

    // UC-D04
    @Override
    public List<CategoryAmountResponse> getIncomesByCategory(
            UUID userId, LocalDate startDate, LocalDate endDate) {
        return buildEnrichedCategoryList(
                transactionRepository.sumByCategoryAndTypeEnriched(
                        userId, TransactionType.INCOME.name(), startDate, endDate));
    }

    // UC-D05
    @Override
    public List<MonthlySummaryResponse> getMonthlyEvolution(UUID userId) {
        List<Object[]> rows = transactionRepository.findMonthlyEvolution(userId);
        List<MonthlySummaryResponse> result = new ArrayList<>();

        // Étape 1 : consolider income, expense et count par mois
        rows.forEach(row -> {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            TransactionType type = TransactionType.valueOf(row[2].toString());
            BigDecimal amount = new BigDecimal(row[3].toString());
            long count = ((Number) row[4]).longValue();

            MonthlySummaryResponse existing = result.stream()
                    .filter(r -> r.getYear() == year && r.getMonth() == month)
                    .findFirst().orElse(null);

            if (existing == null) {
                // Initialiser le mois avec des zéros
                MonthlySummaryResponse newMonth = new MonthlySummaryResponse(
                        year, month,
                        BigDecimal.ZERO,  // totalIncome
                        BigDecimal.ZERO,  // totalExpense
                        BigDecimal.ZERO,  // balance
                        0L,               // transactionCount
                        BigDecimal.ZERO   // savingsRate
                );
                result.add(newMonth);
                existing = newMonth;
            }

            // Mettre à jour les valeurs selon le type
            if (type == TransactionType.INCOME) {
                existing.setTotalIncome(amount);
            } else {
                existing.setTotalExpense(amount);
            }
            existing.setTransactionCount(existing.getTransactionCount() + count);
        });

        // Étape 2 : calculer balance et savingsRate UNE SEULE FOIS
        // quand TOUS les montants sont consolidés
        result.forEach(r -> {
            // balance = income - expense
            r.setBalance(r.getTotalIncome().subtract(r.getTotalExpense()));

            // savingsRate = (balance / income) * 100
            if (r.getTotalIncome().compareTo(BigDecimal.ZERO) > 0) {
                r.setSavingsRate(
                        r.getBalance()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(r.getTotalIncome(), 2, RoundingMode.HALF_UP));
            }
        });

        return result;
    }

    // UC-D06
    @Override
    public SavingsGoalsSummaryResponse getSavingsGoalsSummary(UUID userId) {
        long total = savingsGoalRepository.countByUserId(userId);
        long completed = savingsGoalRepository.countByUserIdAndStatus(
                userId, SavingsGoalStatus.COMPLETED);
        long failed = savingsGoalRepository.countByUserIdAndStatus(
                userId, SavingsGoalStatus.FAILED);
        long cancelled = savingsGoalRepository.countByUserIdAndStatus(
                userId, SavingsGoalStatus.CANCELLED);
        long inProgress = savingsGoalRepository.countByUserIdAndStatus(
                userId, SavingsGoalStatus.IN_PROGRESS);

        BigDecimal totalTarget = nullSafe(
                savingsGoalRepository.sumTargetAmountByUserId(userId));
        BigDecimal totalCurrent = nullSafe(
                savingsGoalRepository.sumCurrentAmountByUserId(userId));

        BigDecimal globalProgress = BigDecimal.ZERO;
        if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
            globalProgress = totalCurrent
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalTarget, 2, RoundingMode.HALF_UP);
        }

        return new SavingsGoalsSummaryResponse(
                total, completed, failed, cancelled, inProgress,
                totalTarget, totalCurrent, globalProgress);
    }

    // UC-D07
    @Override
    public List<TopTransactionResponse> getTopExpenses(
            UUID userId, int year, int month, int limit) {
        return buildTopTransactionList(
                transactionRepository.findTopTransactionsByMonth(
                        userId, TransactionType.EXPENSE.name(), year, month, limit));
    }

    @Override
    public List<TopTransactionResponse> getTopIncomes(
            UUID userId, int year, int month, int limit) {
        return buildTopTransactionList(
                transactionRepository.findTopTransactionsByMonth(
                        userId, TransactionType.INCOME.name(), year, month, limit));
    }

    // UC-D08
    @Override
    public List<MonthlySummaryResponse> getYearlySummary(UUID userId, int year) {
        List<Object[]> rows = transactionRepository.findYearlySummary(userId, year);
        List<MonthlySummaryResponse> result = new ArrayList<>();

        // Initialiser les 12 mois à zéro
        for (int m = 1; m <= 12; m++) {
            result.add(new MonthlySummaryResponse(
                    year, m, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, 0L, BigDecimal.ZERO));
        }

        rows.forEach(row -> {
            int month = ((Number) row[0]).intValue();
            TransactionType type = TransactionType.valueOf(row[1].toString());
            BigDecimal amount = new BigDecimal(row[2].toString());
            long count = ((Number) row[3]).longValue();

            MonthlySummaryResponse existing = result.get(month - 1);

            if (type == TransactionType.INCOME) {
                existing.setTotalIncome(amount);
                existing.setTransactionCount(
                        existing.getTransactionCount() + count);
            } else {
                existing.setTotalExpense(amount);
                existing.setTransactionCount(
                        existing.getTransactionCount() + count);
            }
            existing.setBalance(
                    existing.getTotalIncome().subtract(existing.getTotalExpense()));

            // Calcul savingsRate
            if (existing.getTotalIncome().compareTo(BigDecimal.ZERO) > 0) {
                existing.setSavingsRate(
                        existing.getBalance()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(existing.getTotalIncome(), 2, RoundingMode.HALF_UP));
            }
        });

        return result;
    }

    // ===== Méthodes utilitaires =====

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private List<TopTransactionResponse> buildTopTransactionList(List<Object[]> rows) {
        List<TopTransactionResponse> result = new ArrayList<>();
        rows.forEach(row -> result.add(new TopTransactionResponse(
                UUID.fromString(row[0].toString()),
                row[1].toString(),
                new BigDecimal(row[2].toString()),
                TransactionType.valueOf(row[3].toString()),
                row[4].toString(),
                java.time.LocalDate.parse(row[5].toString())
        )));
        return result;
    }

    private List<CategoryAmountResponse> buildEnrichedCategoryList(List<Object[]> rows) {
        // Calculer le total pour les pourcentages
        BigDecimal grandTotal = rows.stream()
                .map(row -> new BigDecimal(row[2].toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryAmountResponse> result = new ArrayList<>();
        rows.forEach(row -> {
            BigDecimal amount = new BigDecimal(row[2].toString());
            long count = ((Number) row[3]).longValue();

            BigDecimal percentage = BigDecimal.ZERO;
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount
                        .multiply(BigDecimal.valueOf(100))
                        .divide(grandTotal, 2, RoundingMode.HALF_UP);
            }

            result.add(new CategoryAmountResponse(
                    UUID.fromString(row[0].toString()),
                    row[1].toString(),
                    amount,
                    percentage,
                    count
            ));
        });
        return result;
    }
}