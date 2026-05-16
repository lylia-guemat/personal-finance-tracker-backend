package com.dauphine.pft.services;

import com.dauphine.pft.dto.responses.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DashboardService {

    // UC-D01 — Résumé global
    DashboardSummaryResponse getSummary(UUID userId);

    // UC-D02 — Résumé mensuel
    MonthlySummaryResponse getMonthlySummary(UUID userId, int year, int month);

    // UC-D03 — Dépenses par catégorie avec filtres dates optionnels
    List<CategoryAmountResponse> getExpensesByCategory(
            UUID userId, LocalDate startDate, LocalDate endDate);

    // UC-D04 — Revenus par catégorie avec filtres dates optionnels
    List<CategoryAmountResponse> getIncomesByCategory(
            UUID userId, LocalDate startDate, LocalDate endDate);

    // UC-D05 — Évolution mensuelle
    List<MonthlySummaryResponse> getMonthlyEvolution(UUID userId);

    // UC-D06 — Progression globale des savings goals
    SavingsGoalsSummaryResponse getSavingsGoalsSummary(UUID userId);

    // UC-D07 — Top N dépenses du mois
    List<TopTransactionResponse> getTopExpenses(UUID userId, int year, int month, int limit);

    // UC-D07 — Top N revenus du mois
    List<TopTransactionResponse> getTopIncomes(UUID userId, int year, int month, int limit);

    // UC-D08 — Résumé annuel mois par mois
    List<MonthlySummaryResponse> getYearlySummary(UUID userId, int year);
}
