package com.mediflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medical_services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // Ajouté pour le calcul du temps d'attente estimé
    @Column(name = "average_consultation_time")
    private Integer averageConsultationTime;

    private Boolean active = true;
}