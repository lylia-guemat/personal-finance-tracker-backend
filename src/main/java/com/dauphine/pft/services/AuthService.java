package com.dauphine.pft.services;

import com.dauphine.pft.dto.requests.LoginRequest;
import com.dauphine.pft.dto.requests.RegisterRequest;
import com.dauphine.pft.dto.responses.AuthResponse;
import com.dauphine.pft.models.AppUser;

public interface AuthService {

    // Inscription
    AuthResponse register(RegisterRequest request);

    // Connexion
    AuthResponse login(LoginRequest request);

    // Récupérer l'utilisateur connecté
    AppUser getCurrentUser(String token);
}
