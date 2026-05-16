package com.dauphine.pft.services;

import com.dauphine.pft.models.Category;
import com.dauphine.pft.models.CategoryType;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    // UC-C04 — Récupérer toutes les catégories de l'utilisateur connecté
    List<Category> getAllByUser(UUID userId);

    // UC-C06 — Rechercher par nom
    List<Category> searchByName(UUID userId, String name);

    // UC-C01 — Filtrer par type
    List<Category> getAllByUserAndType(UUID userId, CategoryType type);

    // UC-C05 — Récupérer une catégorie par id
    Category getByIdAndUser(UUID id, UUID userId);

    // UC-C01 — Créer une catégorie
    Category create(UUID userId, String name, String description, CategoryType type);

    // UC-C02 — Modifier une catégorie
    Category update(UUID id, UUID userId, String name, String description, CategoryType type);

    // UC-C03 — Supprimer une catégorie (vérifie qu'elle n'est pas utilisée)
    void delete(UUID id, UUID userId);
}
