package com.dauphine.pft.repositories;


import com.dauphine.pft.models.Transaction;
import com.dauphine.pft.models.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Toutes les transactions d'un utilisateur, triées par date décroissante
    List<Transaction> findByUserIdOrderByTransactionDateDesc(UUID userId);

    // Récupérer une transaction par id et userId
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    // Filtre combiné (tous les critères sont optionnels)
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.user.id = :userId
        AND (:type IS NULL OR t.type = :type)
        AND (:categoryId IS NULL OR t.category.id = :categoryId)
        AND (:startDate IS NULL OR t.transactionDate >= :startDate)
        AND (:endDate IS NULL OR t.transactionDate <= :endDate)
        AND (:minAmount IS NULL OR t.amount >= :minAmount)
        AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
        AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY t.transactionDate DESC
    """)
    List<Transaction> findWithFilters(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("categoryId") UUID categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("keyword") String keyword
    );

    // Pour le dashboard : total par type pour un utilisateur
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
        AND t.type = :type
    """)
    BigDecimal sumByUserIdAndType(@Param("userId") UUID userId, @Param("type") TransactionType type);

    // Pour le dashboard : total par type et par mois
    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
    AND t.type = :type
    AND EXTRACT(YEAR FROM t.transactionDate) = :year
    AND EXTRACT(MONTH FROM t.transactionDate) = :month
""")
    BigDecimal sumByUserIdAndTypeAndMonth(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("year") int year,
            @Param("month") int month
    );

    // Pour le dashboard : dépenses/revenus groupés par catégorie
    @Query("""
        SELECT t.category.name, COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
        AND t.type = :type
        GROUP BY t.category.name
        ORDER BY SUM(t.amount) DESC
    """)
    List<Object[]> sumByCategoryAndType(@Param("userId") UUID userId, @Param("type") TransactionType type);

    // Pour le dashboard : évolution mensuelle revenus/dépenses
    @Query("""
        SELECT EXTRACT(YEAR FROM t.transactionDate), 
               EXTRACT(MONTH FROM t.transactionDate),
               t.type,
               COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
        GROUP BY EXTRACT(YEAR FROM t.transactionDate),
                 EXTRACT(MONTH FROM t.transactionDate),
                 t.type
        ORDER BY EXTRACT(YEAR FROM t.transactionDate),
                 EXTRACT(MONTH FROM t.transactionDate)
    """)
    List<Object[]> findMonthlyEvolution(@Param("userId") UUID userId);

    // Nombre total de transactions d'un utilisateur
    long countByUserId(UUID userId);
}
