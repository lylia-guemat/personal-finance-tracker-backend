package com.dauphine.pft.repositories;

import com.dauphine.pft.models.Category;
import com.dauphine.pft.models.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // Toutes les catégories d'un utilisateur
    List<Category> findByUserId(UUID userId);

    // Filtrer par type pour un utilisateur
    List<Category> findByUserIdAndType(UUID userId, CategoryType type);

    // Recherche par nom pour un utilisateur
    List<Category> findByUserIdAndNameContainingIgnoreCase(UUID userId, String name);

    // Vérifier si une catégorie existe déjà (même nom + même type + même user)
    boolean existsByUserIdAndNameIgnoreCaseAndType(UUID userId, String name, CategoryType type);

    // Vérifier si une catégorie est utilisée par des transactions
    boolean existsByIdAndTransactionsIsNotEmpty(UUID id);

    // Récupérer une catégorie par id et userId
    Optional<Category> findByIdAndUserId(UUID id, UUID userId);
}
