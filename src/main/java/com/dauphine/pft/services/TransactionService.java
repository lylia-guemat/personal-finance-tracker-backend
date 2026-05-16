package com.dauphine.pft.services;

import com.dauphine.pft.models.Transaction;
import com.dauphine.pft.models.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    // UC-T04 — Toutes les transactions de l'utilisateur
    List<Transaction> getAllByUser(UUID userId);

    // UC-T05 — Une transaction par id
    Transaction getByIdAndUser(UUID id, UUID userId);

    // UC-T06/T07 — Filtrer avec tous les critères combinables
    List<Transaction> filter(
            UUID userId,
            TransactionType type,
            UUID categoryId,
            String categoryName,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String keyword
    );

    // UC-T01 — Créer une transaction
    Transaction create(
            UUID userId,
            String title,
            String description,
            BigDecimal amount,
            TransactionType type,
            LocalDate transactionDate,
            UUID categoryId
    );

    // UC-T02 — Modifier une transaction
    Transaction update(
            UUID id,
            UUID userId,
            String title,
            String description,
            BigDecimal amount,
            TransactionType type,
            LocalDate transactionDate,
            UUID categoryId
    );

    // UC-T03 — Supprimer une transaction
    void delete(UUID id, UUID userId);
}
