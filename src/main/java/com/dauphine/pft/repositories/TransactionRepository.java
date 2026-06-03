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
    @Query(value = """
    SELECT t.* FROM finance_transaction t
    JOIN category c ON c.id = t.category_id
    WHERE t.user_id = :userId
    AND (CAST(:type AS VARCHAR) IS NULL OR t.type = CAST(:type AS VARCHAR))
    AND (CAST(:categoryId AS UUID) IS NULL OR t.category_id = CAST(:categoryId AS UUID))
    AND (:categoryName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%')))
    AND (CAST(:startDate AS DATE) IS NULL OR t.transaction_date >= CAST(:startDate AS DATE))
    AND (CAST(:endDate AS DATE) IS NULL OR t.transaction_date <= CAST(:endDate AS DATE))
    AND (CAST(:minAmount AS NUMERIC) IS NULL OR t.amount >= CAST(:minAmount AS NUMERIC))
    AND (CAST(:maxAmount AS NUMERIC) IS NULL OR t.amount <= CAST(:maxAmount AS NUMERIC))
    AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY t.transaction_date DESC
    """, nativeQuery = true)
    List<Transaction> findWithFilters(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("categoryId") UUID categoryId,
            @Param("categoryName") String categoryName,
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
        SELECT t.category.id, t.category.name, COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
        AND t.type = :type
        GROUP BY t.category.id, t.category.name
        ORDER BY SUM(t.amount) DESC
    """)
    List<Object[]> sumByCategoryAndType(@Param("userId") UUID userId, @Param("type") TransactionType type);

    // Pour le dashboard : évolution mensuelle revenus/dépenses
    @Query(value = """
    SELECT 
        EXTRACT(YEAR FROM t.transaction_date) as year,
        EXTRACT(MONTH FROM t.transaction_date) as month,
        t.type,
        COALESCE(SUM(t.amount), 0) as total,
        COUNT(t.id) as transaction_count
    FROM finance_transaction t
    WHERE t.user_id = :userId
    GROUP BY EXTRACT(YEAR FROM t.transaction_date),
             EXTRACT(MONTH FROM t.transaction_date),
             t.type
    ORDER BY EXTRACT(YEAR FROM t.transaction_date),
             EXTRACT(MONTH FROM t.transaction_date)
    """, nativeQuery = true)
    List<Object[]> findMonthlyEvolution(@Param("userId") UUID userId);

    // Nombre total de transactions d'un utilisateur
    long countByUserId(UUID userId);

    // UC-D07 : Top N transactions par type et par mois
    @Query(value = """
    SELECT t.id, t.title, t.amount, t.type, c.name as category_name, t.transaction_date
    FROM finance_transaction t
    JOIN category c ON c.id = t.category_id
    WHERE t.user_id = :userId
    AND t.type = :type
    AND EXTRACT(YEAR FROM t.transaction_date) = :year
    AND EXTRACT(MONTH FROM t.transaction_date) = :month
    ORDER BY t.amount DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopTransactionsByMonth(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("year") int year,
            @Param("month") int month,
            @Param("limit") int limit
    );

    // UC-D08 : Résumé annuel mois par mois
    @Query(value = """
    SELECT 
        EXTRACT(MONTH FROM t.transaction_date) as month,
        t.type,
        COALESCE(SUM(t.amount), 0) as total,
        COUNT(t.id) as transaction_count
    FROM finance_transaction t
    WHERE t.user_id = :userId
    AND EXTRACT(YEAR FROM t.transaction_date) = :year
    GROUP BY EXTRACT(MONTH FROM t.transaction_date), t.type
    ORDER BY EXTRACT(MONTH FROM t.transaction_date)
    """, nativeQuery = true)
    List<Object[]> findYearlySummary(
            @Param("userId") UUID userId,
            @Param("year") int year
    );

    // UC-D03/D04 enrichi : total + count par catégorie
    @Query(value = """
    SELECT t.category_id, c.name, 
           COALESCE(SUM(t.amount), 0) as total_amount,
           COUNT(t.id) as transaction_count
    FROM finance_transaction t
    JOIN category c ON c.id = t.category_id
    WHERE t.user_id = :userId
    AND t.type = :type
    AND (CAST(:startDate AS DATE) IS NULL OR t.transaction_date >= CAST(:startDate AS DATE))
    AND (CAST(:endDate AS DATE) IS NULL OR t.transaction_date <= CAST(:endDate AS DATE))
    GROUP BY t.category_id, c.name
    ORDER BY total_amount DESC
    """, nativeQuery = true)
    List<Object[]> sumByCategoryAndTypeEnriched(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
        AND t.category.id = :categoryId
        AND t.type = com.dauphine.pft.models.TransactionType.EXPENSE
        AND t.transactionDate >= :startDate
        AND t.transactionDate <= :endDate
    """)
    BigDecimal sumExpensesByUserAndCategoryBetween(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Nombre de transactions par mois
    @Query(value = """
    SELECT COUNT(*)
    FROM finance_transaction t
    WHERE t.user_id = :userId
    AND EXTRACT(YEAR FROM t.transaction_date) = :year
    AND EXTRACT(MONTH FROM t.transaction_date) = :month
    """, nativeQuery = true)
    long countByUserIdAndMonth(
            @Param("userId") UUID userId,
            @Param("year") int year,
            @Param("month") int month
    );
}
