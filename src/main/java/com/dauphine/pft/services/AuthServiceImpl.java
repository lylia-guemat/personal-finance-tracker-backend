package com.dauphine.pft.services;

import com.dauphine.pft.dto.requests.LoginRequest;
import com.dauphine.pft.dto.requests.RegisterRequest;
import com.dauphine.pft.dto.responses.AuthResponse;
import com.dauphine.pft.exceptions.InvalidCredentialsException;
import com.dauphine.pft.exceptions.UserAlreadyExistsException;
import com.dauphine.pft.models.AppUser;
import com.dauphine.pft.repositories.AppUserRepository;
import com.dauphine.pft.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AppUserRepository appUserRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Vérifier que l'email n'existe pas déjà
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        // Créer l'utilisateur avec le mot de passe hashé
        AppUser user = new AppUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        appUserRepository.save(user);

        // Générer et retourner le token
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(
                token,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Chercher l'utilisateur par email
        AppUser user = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // Générer et retourner le token
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(
                token,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
    }

    @Override
    public AppUser getCurrentUser(String token) {
        String cleanToken = token.startsWith("Bearer ")
                ? token.substring(7) : token;

        if (!jwtUtils.validateToken(cleanToken)) {
            throw new com.dauphine.pft.exceptions.InvalidTokenException();
        }

        String email = jwtUtils.extractEmail(cleanToken);
        return appUserRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
    }
}
