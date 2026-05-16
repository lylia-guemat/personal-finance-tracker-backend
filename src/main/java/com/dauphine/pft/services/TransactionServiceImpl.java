package com.dauphine.pft.services;

import com.dauphine.pft.exceptions.*;
import com.dauphine.pft.models.*;
import com.dauphine.pft.repositories.AppUserRepository;
import com.dauphine.pft.repositories.CategoryRepository;
import com.dauphine.pft.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository appUserRepository;

    // UC-T04
    @Override
    public List<Transaction> getAllByUser(UUID userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
    }

    // UC-T05
    @Override
    public Transaction getByIdAndUser(UUID id, UUID userId) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    // UC-T06/T07 — tous les filtres combinables
    @Override
    public List<Transaction> filter(UUID userId, TransactionType type, UUID categoryId,
                                    String categoryName, LocalDate startDate, LocalDate endDate,
                                    BigDecimal minAmount, BigDecimal maxAmount, String keyword) {

        String cleanCategoryName = (categoryName != null && !categoryName.isBlank())
                ? categoryName : null;
        String cleanKeyword = (keyword != null && !keyword.isBlank())
                ? keyword : null;

        // Convertir l'enum en String pour la requête native (ou null si pas de filtre)
        String typeStr = type != null ? type.name() : null;

        // Si AUCUN filtre → requête simple
        boolean hasFilters = type != null || categoryId != null
                || cleanCategoryName != null || startDate != null
                || endDate != null || minAmount != null
                || maxAmount != null || cleanKeyword != null;

        if (!hasFilters) {
            return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        }

        return transactionRepository.findWithFilters(
                userId, typeStr, categoryId, cleanCategoryName,
                startDate, endDate, minAmount, maxAmount, cleanKeyword);
    }

    // UC-T01
    @Override
    public Transaction create(UUID userId, String title, String description,
                              BigDecimal amount, TransactionType type,
                              LocalDate transactionDate, UUID categoryId) {

        // Vérifier que la date n'est pas dans le futur
        if (transactionDate.isAfter(LocalDate.now())) {
            throw new InvalidTransactionDateException();
        }

        // Vérifier que le montant est positif
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException();
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier que la catégorie appartient à l'utilisateur
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // Vérifier que le type de catégorie est compatible avec le type de transaction
        if (category.getType() != CategoryType.BOTH
                && !category.getType().name().equals(type.name())) {
            throw new CategoryTypeMismatchException();
        }

        Transaction transaction = new Transaction();
        transaction.setTitle(title);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTransactionDate(transactionDate);
        transaction.setCategory(category);
        transaction.setUser(user);

        return transactionRepository.save(transaction);
    }

    // UC-T02
    @Override
    public Transaction update(UUID id, UUID userId, String title, String description,
                              BigDecimal amount, TransactionType type,
                              LocalDate transactionDate, UUID categoryId) {

        // Vérifier que la transaction appartient à l'utilisateur
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        if (transactionDate.isAfter(LocalDate.now())) {
            throw new InvalidTransactionDateException();
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException();
        }

        // Vérifier que la catégorie appartient à l'utilisateur
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        if (category.getType() != CategoryType.BOTH
                && !category.getType().name().equals(type.name())) {
            throw new CategoryTypeMismatchException();
        }

        transaction.setTitle(title);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTransactionDate(transactionDate);
        transaction.setCategory(category);

        return transactionRepository.save(transaction);
    }

    // UC-T03
    @Override
    public void delete(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        transactionRepository.delete(transaction);
    }
}