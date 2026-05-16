package com.dauphine.pft.services;

import com.dauphine.pft.models.AppUser;
import com.dauphine.pft.models.Category;
import com.dauphine.pft.exceptions.*;
import com.dauphine.pft.models.CategoryType;
import com.dauphine.pft.repositories.AppUserRepository;
import com.dauphine.pft.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public List<Category> getAllByUser(UUID userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Override
    public List<Category> searchByName(UUID userId, String name) {
        return categoryRepository.findByUserIdAndNameContainingIgnoreCase(userId, name);
    }

    @Override
    public List<Category> getAllByUserAndType(UUID userId, CategoryType type) {
        return categoryRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public Category getByIdAndUser(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    public Category create(UUID userId, String name, String description, CategoryType type) {
        // UC-C01 : vérifier qu'une catégorie avec le même nom et type n'existe pas déjà
        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, name, type)) {
            throw new CategoryAlreadyExistsException(name);
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setType(type);
        category.setUser(user);

        return categoryRepository.save(category);
    }

    @Override
    public Category update(UUID id, UUID userId, String name, String description, CategoryType type) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Vérifier si le nouveau nom+type crée un doublon (sauf si c'est la même catégorie)
        if (!category.getName().equalsIgnoreCase(name) || category.getType() != type) {
            if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, name, type)) {
                throw new CategoryAlreadyExistsException(name);
            }
        }

        category.setName(name);
        category.setDescription(description);
        category.setType(type);

        return categoryRepository.save(category);
    }

    @Override
    public void delete(UUID id, UUID userId) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // UC-C03 : interdire la suppression si la catégorie est utilisée
        if (categoryRepository.existsByIdAndTransactionsIsNotEmpty(id)) {
            throw new CategoryInUseException(id);
        }

        categoryRepository.delete(category);
    }
}
