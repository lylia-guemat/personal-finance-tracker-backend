package com.dauphine.pft.controllers;

import com.dauphine.pft.dto.requests.SavingsGoalContributionRequest;
import com.dauphine.pft.dto.requests.SavingsGoalRequest;
import com.dauphine.pft.dto.requests.UpdateSavingsGoalProgressRequest;
import com.dauphine.pft.dto.responses.SavingsGoalContributionResponse;
import com.dauphine.pft.dto.responses.SavingsGoalResponse;
import com.dauphine.pft.mappers.SavingsGoalContributionMapper;
import com.dauphine.pft.mappers.SavingsGoalMapper;
import com.dauphine.pft.models.SavingsGoalStatus;
import com.dauphine.pft.security.SecurityUtils;
import com.dauphine.pft.services.SavingsGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/savings-goals")
@RequiredArgsConstructor
@Tag(name = "Savings Goals API", description = "Endpoints for managing savings goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final SavingsGoalMapper savingsGoalMapper;
    private final SavingsGoalContributionMapper savingsGoalContributionMapper;

    private UUID CURRENT_USER_ID() {
        return SecurityUtils.getCurrentUserId();
    }

    // UC-G04 - Get all + filters
    @GetMapping
    @Operation(
            summary = "Get all savings goals",
            description = "Returns all savings goals for the connected user. " +
                    "Filters are optional and combinable. " +
                    "Automatically marks overdue goals as FAILED.")
    public ResponseEntity<List<SavingsGoalResponse>> getAll(
            @Parameter(description = "Filter by status : IN_PROGRESS, COMPLETED, CANCELLED, FAILED")
            @RequestParam(required = false) SavingsGoalStatus status,

            @Parameter(description = "Search keyword in name or description")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Filter by minimum target amount")
            @RequestParam(name = "min-target", required = false) BigDecimal minTarget,

            @Parameter(description = "Filter by maximum target amount")
            @RequestParam(name = "max-target", required = false) BigDecimal maxTarget,

            @Parameter(description = "Filter by deadline before date (yyyy-MM-dd)")
            @RequestParam(name = "deadline-before", required = false) LocalDate deadlineBefore,

            @Parameter(description = "Filter by deadline after date (yyyy-MM-dd)")
            @RequestParam(name = "deadline-after", required = false) LocalDate deadlineAfter) {

        savingsGoalService.markOverdueAsFailed(CURRENT_USER_ID());

        List<SavingsGoalResponse> goals = savingsGoalService
                .filter(CURRENT_USER_ID(), status, keyword,
                        minTarget, maxTarget, deadlineBefore, deadlineAfter)
                .stream().map(savingsGoalMapper::toResponse).toList();

        return ResponseEntity.ok(goals);
    }

    // UC-G05 - Get by id
    @GetMapping("/{id}")
    @Operation(summary = "Get savings goal by ID")
    public ResponseEntity<SavingsGoalResponse> getById(
            @Parameter(description = "Savings goal UUID") @PathVariable UUID id) {

        savingsGoalService.markOverdueAsFailed(CURRENT_USER_ID());

        return ResponseEntity.ok(
                savingsGoalMapper.toResponse(
                        savingsGoalService.getByIdAndUser(id, CURRENT_USER_ID())));
    }

    // UC-G01 - Create
    @PostMapping
    @Operation(
            summary = "Create a new savings goal",
            description = "Creates a new savings goal. " +
                    "currentAmount cannot exceed targetAmount. " +
                    "Status is automatically set to COMPLETED if currentAmount equals targetAmount. " +
                    "Status is automatically set to FAILED if deadline is passed.")
    public ResponseEntity<SavingsGoalResponse> create(
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse response = savingsGoalMapper.toResponse(
                savingsGoalService.create(
                        CURRENT_USER_ID(),
                        request.getName(),
                        request.getDescription(),
                        request.getTargetAmount(),
                        request.getCurrentAmount(),
                        request.getDeadline()));
        return ResponseEntity
                .created(URI.create("/v1/savings-goals/" + response.getId()))
                .body(response);
    }

    // UC-G02 - Update
    @PutMapping("/{id}")
    @Operation(
            summary = "Update a savings goal",
            description = "Updates a savings goal. " +
                    "Cannot update a COMPLETED or FAILED goal. " +
                    "Status can be set to IN_PROGRESS or CANCELLED.")
    public ResponseEntity<SavingsGoalResponse> update(
            @Parameter(description = "Savings goal UUID") @PathVariable UUID id,
            @Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.ok(
                savingsGoalMapper.toResponse(
                        savingsGoalService.update(
                                id,
                                CURRENT_USER_ID(),
                                request.getName(),
                                request.getDescription(),
                                request.getTargetAmount(),
                                request.getCurrentAmount(),
                                request.getDeadline(),
                                request.getStatus())));
    }

    // UC-G06 - Update progress only
    @PatchMapping("/{id}/progress")
    @Operation(
            summary = "Add amount to savings goal progress",
            description = "Adds the given amount to the current savings amount. " +
                    "For example if currentAmount is 50 and you send 20, result will be 70. " +
                    "Automatically sets status to COMPLETED if total reaches targetAmount.")
    public ResponseEntity<SavingsGoalResponse> updateProgress(
            @Parameter(description = "Savings goal UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateSavingsGoalProgressRequest request) {
        return ResponseEntity.ok(
                savingsGoalMapper.toResponse(
                        savingsGoalService.updateProgress(
                                id,
                                CURRENT_USER_ID(),
                                request.getCurrentAmount())));
    }

    @GetMapping("/{id}/contributions")
    @Operation(summary = "Get savings goal contributions")
    public ResponseEntity<List<SavingsGoalContributionResponse>> getContributions(
            @Parameter(description = "Savings goal UUID") @PathVariable UUID id) {
        List<SavingsGoalContributionResponse> contributions = savingsGoalService
                .getContributions(id, CURRENT_USER_ID())
                .stream()
                .map(savingsGoalContributionMapper::toResponse)
                .toList();

        return ResponseEntity.ok(contributions);
    }

    @PostMapping("/{id}/contributions")
    @Operation(
            summary = "Add a contribution to a savings goal",
            description = "Creates a contribution and increments currentAmount. " +
                    "Automatically marks the goal as COMPLETED when targetAmount is reached.")
    public ResponseEntity<SavingsGoalContributionResponse> contribute(
            @Parameter(description = "Savings goal UUID") @PathVariable UUID id,
            @Valid @RequestBody SavingsGoalContributionRequest request) {
        SavingsGoalContributionResponse response = savingsGoalContributionMapper.toResponse(
                savingsGoalService.contribute(
                        id,
                        CURRENT_USER_ID(),
                        request.getAmount(),
                        request.getDate(),
                        request.getNote()));

        return ResponseEntity
                .created(URI.create("/v1/savings-goals/" + id + "/contributions/" + response.getId()))
                .body(response);
    }

    // UC-G03 - Delete
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a savings goal")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Savings goal UUID") @PathVariable UUID id) {
        savingsGoalService.delete(id, CURRENT_USER_ID());
        return ResponseEntity.noContent().build();
    }
}
