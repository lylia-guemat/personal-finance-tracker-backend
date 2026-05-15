package com.dauphine.pft.repositories;

import com.dauphine.pft.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    // Chercher un utilisateur par email (pour le login)
    Optional<AppUser> findByEmail(String email);

    // Vérifier si un email existe déjà (pour l'inscription)
    boolean existsByEmail(String email);
}
