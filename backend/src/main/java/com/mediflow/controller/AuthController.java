package com.mediflow.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediflow.entity.User;
import com.mediflow.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permet à ton application JavaFX de communiquer avec le serveur
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> user = authService.authenticate(email, password);

        if (user.isPresent()) {
            // On renvoie les infos de l'utilisateur (sans le mot de passe pour la sécurité)
            User authenticatedUser = user.get();
            return ResponseEntity.ok(Map.of(
                "id", authenticatedUser.getId(),
                "fullName", authenticatedUser.getFullName(),
                "role", authenticatedUser.getRole()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Email ou mot de passe incorrect.");
        }
    }
}