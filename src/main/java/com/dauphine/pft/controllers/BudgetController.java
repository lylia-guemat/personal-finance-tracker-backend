package com.dauphine.pft.controllers;

import com.dauphine.pft.dto.requests.BudgetRequest;
import com.dauphine.pft.dto.requests.BudgetUpdateRequest;
import com.dauphine.pft.dto.responses.BudgetUsageResponse;
import com.dauphine.pft.mappers.BudgetMapper;
import com.dauphine.pft.models.Budget;
import com.dauphine.pft.security.SecurityUtils;
import com.dauphine.pft.services.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets API", description = "Endpoints for monthly budget tracking")
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetMapper budgetMapper;

    private UUID CURRENT_USER_ID() {
        return SecurityUtils.getCurrentUserId();
    }

    @GetMapping
    @Operation(summary = "Get monthly budgets with current usage")
    public ResponseEntity<List<BudgetUsageResponse>> getAll() {
        UUID userId = CURRENT_USER_ID();
        List<BudgetUsageResponse> budgets = budgetService.getAllByUser(userId)
                .stream()
                .map(budget -> budgetMapper.toUsageResponse(
                        budget,
                        budgetService.getCurrentMonthSpent(userId, budget.getCategory().getId())))
                .toList();

        return ResponseEntity.ok(budgets);
    }

    @PostMapping
    @Operation(summary = "Create or replace a monthly budget for a category")
    public ResponseEntity<BudgetUsageResponse> create(@Valid @RequestBody BudgetRequest request) {
        UUID userId = CURRENT_USER_ID();
        Budget budget = budgetService.create(userId, request.getCategoryId(), request.getMonthlyCap());
        BudgetUsageResponse response = budgetMapper.toUsageResponse(
                budget,
                budgetService.getCurrentMonthSpent(userId, budget.getCategory().getId()));

        return ResponseEntity
                .created(URI.create("/v1/budgets/" + response.getId()))
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a monthly budget cap")
    public ResponseEntity<BudgetUsageResponse> update(
            @Parameter(description = "Budget UUID") @PathVariable UUID id,
            @Valid @RequestBody BudgetUpdateRequest request) {
        UUID userId = CURRENT_USER_ID();
        Budget budget = budgetService.update(id, userId, request.getMonthlyCap());

        return ResponseEntity.ok(budgetMapper.toUsageResponse(
                budget,
                budgetService.getCurrentMonthSpent(userId, budget.getCategory().getId())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a monthly budget")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Budget UUID") @PathVariable UUID id) {
        budgetService.delete(id, CURRENT_USER_ID());
        return ResponseEntity.noContent().build();
    }
}
