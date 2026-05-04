package com.mediflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mediflow.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Cette méthode nous permettra de chercher l'utilisateur par son email lors du login
    Optional<User> findByEmail(String email);
}