package com.dauphine.pft.controllers;

import com.dauphine.pft.dto.requests.LoginRequest;
import com.dauphine.pft.dto.requests.RegisterRequest;
import com.dauphine.pft.dto.responses.AuthResponse;
import com.dauphine.pft.models.AppUser;
import com.dauphine.pft.repositories.AppUserRepository;
import com.dauphine.pft.security.SecurityUtils;
import com.dauphine.pft.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "Endpoints for authentication")
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository appUserRepository;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new account and returns a JWT token.")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticates a user and returns a JWT token.")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = "Logout the current user. " +
                    "The frontend must delete the token on its side.")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    // Plus de @RequestHeader — on utilise le contexte Spring Security
    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Returns the connected user's information.")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        AppUser user = appUserRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(
                new AuthResponse(
                        null,
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail()));
    }
}