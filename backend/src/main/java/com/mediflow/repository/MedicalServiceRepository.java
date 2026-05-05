package com.mediflow.repository;

import com.mediflow.entity.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long> {
    // Permet de trouver un service par son nom (ex: "Médecine Générale")
    MedicalService findByName(String name);
}