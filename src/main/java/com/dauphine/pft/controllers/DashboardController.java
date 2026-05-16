package com.dauphine.pft.controllers;

import com.dauphine.pft.dto.responses.*;
import com.dauphine.pft.security.SecurityUtils;
import com.dauphine.pft.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard API", description = "Endpoints for financial summaries, statistics and visualizations")
public class DashboardController {

    private final DashboardService dashboardService;

    private UUID CURRENT_USER_ID() {
        return SecurityUtils.getCurrentUserId();
    }
    // UC-D01 — Résumé global
    @GetMapping("/summary")
    @Operation(
            summary = "Get global financial summary",
            description = "Returns total income, total expense, balance, " +
                    "transaction count and savings rate for the connected user.")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary(CURRENT_USER_ID()));
    }

    // UC-D02 — Résumé mensuel
    @GetMapping("/monthly-summary")
    @Operation(
            summary = "Get monthly financial summary",
            description = "Returns income, expense, balance, transaction count " +
                    "and savings rate for a specific month.")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @Parameter(description = "Year (ex: 2026)")
            @RequestParam int year,
            @Parameter(description = "Month (1-12)")
            @RequestParam int month) {
        return ResponseEntity.ok(
                dashboardService.getMonthlySummary(CURRENT_USER_ID(), year, month));
    }

    // UC-D03
    @GetMapping("/expenses-by-category")
    @Operation(
            summary = "Get expenses grouped by category",
            description = "Returns total amount, percentage of total and transaction count " +
                    "per expense category. Date filters are optional. " +
                    "Useful for pie/donut charts.")
    public ResponseEntity<List<CategoryAmountResponse>> getExpensesByCategory(
            @Parameter(description = "Filter from date (yyyy-MM-dd)")
            @RequestParam(name = "start-date", required = false) LocalDate startDate,
            @Parameter(description = "Filter to date (yyyy-MM-dd)")
            @RequestParam(name = "end-date", required = false) LocalDate endDate) {
        return ResponseEntity.ok(
                dashboardService.getExpensesByCategory(
                        CURRENT_USER_ID(), startDate, endDate));
    }

    // UC-D04
    @GetMapping("/incomes-by-category")
    @Operation(
            summary = "Get incomes grouped by category",
            description = "Returns total amount, percentage of total and transaction count " +
                    "per income category. Date filters are optional. " +
                    "Useful for pie/donut charts.")
    public ResponseEntity<List<CategoryAmountResponse>> getIncomesByCategory(
            @Parameter(description = "Filter from date (yyyy-MM-dd)")
            @RequestParam(name = "start-date", required = false) LocalDate startDate,
            @Parameter(description = "Filter to date (yyyy-MM-dd)")
            @RequestParam(name = "end-date", required = false) LocalDate endDate) {
        return ResponseEntity.ok(
                dashboardService.getIncomesByCategory(
                        CURRENT_USER_ID(), startDate, endDate));
    }

    // UC-D05 — Évolution mensuelle
    @GetMapping("/monthly-evolution")
    @Operation(
            summary = "Get monthly evolution of income and expenses",
            description = "Returns income, expense and balance for each month. " +
                    "Useful for line/bar charts.")
    public ResponseEntity<List<MonthlySummaryResponse>> getMonthlyEvolution() {
        return ResponseEntity.ok(
                dashboardService.getMonthlyEvolution(CURRENT_USER_ID()));
    }

    // UC-D06 — Progression globale des savings goals
    @GetMapping("/savings-goals-summary")
    @Operation(
            summary = "Get global savings goals summary",
            description = "Returns total goals count by status, " +
                    "total target amount, total current amount and global progress percentage.")
    public ResponseEntity<SavingsGoalsSummaryResponse> getSavingsGoalsSummary() {
        return ResponseEntity.ok(
                dashboardService.getSavingsGoalsSummary(CURRENT_USER_ID()));
    }

    // UC-D07 — Top dépenses du mois
    @GetMapping("/top-expenses")
    @Operation(
            summary = "Get top expenses for a given month",
            description = "Returns the highest expense transactions for a specific month. " +
                    "Default limit is 5.")
    public ResponseEntity<List<TopTransactionResponse>> getTopExpenses(
            @Parameter(description = "Year (ex: 2026)")
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().year}") int year,
            @Parameter(description = "Month (1-12)")
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().monthValue}") int month,
            @Parameter(description = "Number of results (default 5)")
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(
                dashboardService.getTopExpenses(CURRENT_USER_ID(), year, month, limit));
    }

    // UC-D07 — Top revenus du mois
    @GetMapping("/top-incomes")
    @Operation(
            summary = "Get top incomes for a given month",
            description = "Returns the highest income transactions for a specific month. " +
                    "Default limit is 5.")
    public ResponseEntity<List<TopTransactionResponse>> getTopIncomes(
            @Parameter(description = "Year (ex: 2026)")
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().year}") int year,
            @Parameter(description = "Month (1-12)")
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().monthValue}") int month,
            @Parameter(description = "Number of results (default 5)")
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(
                dashboardService.getTopIncomes(CURRENT_USER_ID(), year, month, limit));
    }

    // UC-D08 — Résumé annuel
    @GetMapping("/yearly-summary")
    @Operation(
            summary = "Get yearly summary month by month",
            description = "Returns income, expense, balance, transaction count " +
                    "and savings rate for each month of a given year. " +
                    "All 12 months are returned even if there are no transactions. " +
                    "Useful for annual bar/line charts.")
    public ResponseEntity<List<MonthlySummaryResponse>> getYearlySummary(
            @Parameter(description = "Year (ex: 2026)")
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().year}") int year) {
        return ResponseEntity.ok(
                dashboardService.getYearlySummary(CURRENT_USER_ID(), year));
    }
}
