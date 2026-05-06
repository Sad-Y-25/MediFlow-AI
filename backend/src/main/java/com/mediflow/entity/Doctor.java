package com.mediflow.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a Doctor in the system.
 * A Doctor is linked to a User account and assigned to a specific MedicalService.
 * This separation ensures clear database normalization where users can have different roles,
 * while doctor-specific attributes (like the assigned service) are kept here.
 */
@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The User account associated with this Doctor.
     * We use OneToOne since one doctor corresponds to exactly one user account.
     */
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    /**
     * The Medical Service this doctor works in (e.g., Cardiology).
     * Many doctors can belong to the same service.
     */
    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private MedicalService service;
}
