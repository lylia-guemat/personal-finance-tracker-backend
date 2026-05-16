package com.dauphine.pft.controllers;

import com.dauphine.pft.dto.requests.TransactionRequest;
import com.dauphine.pft.dto.responses.TransactionResponse;
import com.dauphine.pft.mappers.TransactionMapper;
import com.dauphine.pft.models.TransactionType;
import com.dauphine.pft.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dauphine.pft.services.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions API", description = "Endpoints for managing income and expense transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    private UUID CURRENT_USER_ID() {
        return SecurityUtils.getCurrentUserId();
    }
    // Get all + filtres combinables
    @GetMapping
    @Operation(
            summary = "Get all transactions",
            description = "Returns all transactions for the connected user. " +
                    "All filters are optional and combinable : " +
                    "type, categoryId, startDate, endDate, minAmount, maxAmount, keyword")
    public ResponseEntity<List<TransactionResponse>> getAll(
            @Parameter(description = "Filter by type : INCOME or EXPENSE")
            @RequestParam(required = false) TransactionType type,

            @Parameter(description = "Filter by category UUID")
            @RequestParam(name = "category-id", required = false) UUID categoryId,

            @Parameter(description = "Filter by category name")
            @RequestParam(name = "category-name", required = false) String categoryName,

            @Parameter(description = "Filter from date (yyyy-MM-dd)")
            @RequestParam(name = "start-date", required = false) LocalDate startDate,

            @Parameter(description = "Filter to date (yyyy-MM-dd)")
            @RequestParam(name = "end-date", required = false) LocalDate endDate,

            @Parameter(description = "Filter by minimum amount")
            @RequestParam(name = "min-amount", required = false) BigDecimal minAmount,

            @Parameter(description = "Filter by maximum amount")
            @RequestParam(name = "max-amount", required = false) BigDecimal maxAmount,

            @Parameter(description = "Search keyword in title or description")
            @RequestParam(required = false) String keyword) {

        // Si aucun filtre → retourner toutes les transactions
        List<TransactionResponse> transactions = transactionService
                .filter(CURRENT_USER_ID(), type, categoryId, categoryName,
                        startDate, endDate, minAmount, maxAmount, keyword)
                .stream().map(transactionMapper::toResponse).toList();

        return ResponseEntity.ok(transactions);
    }

    // Get by id
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionResponse> getById(
            @Parameter(description = "Transaction UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(
                transactionMapper.toResponse(
                        transactionService.getByIdAndUser(id, CURRENT_USER_ID())));
    }

    // Create
    @PostMapping
    @Operation(summary = "Create a new transaction",
            description = "Creates a new INCOME or EXPENSE transaction. " +
                    "Date cannot be in the future. " +
                    "Category type must match transaction type.")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionMapper.toResponse(
                transactionService.create(
                        CURRENT_USER_ID(),
                        request.getTitle(),
                        request.getDescription(),
                        request.getAmount(),
                        request.getType(),
                        request.getTransactionDate(),
                        request.getCategoryId()));
        return ResponseEntity
                .created(URI.create("/v1/transactions/" + response.getId()))
                .body(response);
    }

    // Update
    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<TransactionResponse> update(
            @Parameter(description = "Transaction UUID") @PathVariable UUID id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(
                transactionMapper.toResponse(
                        transactionService.update(
                                id,
                                CURRENT_USER_ID(),
                                request.getTitle(),
                                request.getDescription(),
                                request.getAmount(),
                                request.getType(),
                                request.getTransactionDate(),
                                request.getCategoryId())));
    }

    // Delete
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Transaction UUID") @PathVariable UUID id) {
        transactionService.delete(id, CURRENT_USER_ID());
        return ResponseEntity.noContent().build();
    }
}
