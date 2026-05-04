package com.mediflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientName;
    private String reason;
    private int urgencyLevel; // De 1 (stable) à 5 (critique)
    private String status;    // WAITING, IN_PROGRESS, COMPLETED
    private LocalDateTime createdAt = LocalDateTime.now();
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}