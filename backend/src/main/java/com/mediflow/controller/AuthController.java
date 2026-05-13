package com.mediflow.controller;

import java.util.HashMap;
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
import com.mediflow.entity.Doctor;
import com.mediflow.service.AuthService;
import com.mediflow.repository.DoctorRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permet à ton application JavaFX de communiquer avec le serveur
public class AuthController {

    private final AuthService authService;
    private final DoctorRepository doctorRepository;

    public AuthController(AuthService authService, DoctorRepository doctorRepository) {
        this.authService = authService;
        this.doctorRepository = doctorRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> user = authService.authenticate(email, password);

        if (user.isPresent()) {
            User authenticatedUser = user.get();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", authenticatedUser.getId());
            responseData.put("fullName", authenticatedUser.getFullName());
            responseData.put("role", authenticatedUser.getRole());

            if ("DOCTOR".equals(authenticatedUser.getRole())) {
                Optional<Doctor> doctorOpt = doctorRepository.findByUserId(authenticatedUser.getId());
                doctorOpt.ifPresent(doc -> responseData.put("doctorId", doc.getId()));
            }

            return ResponseEntity.ok(responseData);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Email ou mot de passe incorrect.");
        }
    }
}