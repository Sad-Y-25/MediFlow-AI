package com.mediflow.entity;


import java.time.LocalDateTime;

public class User {


    private Long id;


    private String fullName;


    private String email;

    private String password;

    private String role; // ADMIN, RECEPTIONIST, DOCTOR, PATIENT

    private Boolean enabled = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Méthode utilitaire demandée dans le diagramme de classes
    public boolean hasRole(String roleName) {
        return this.role.equals(roleName);
    }
}