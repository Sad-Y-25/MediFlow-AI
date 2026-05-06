package com.mediflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_tickets") // Nom exact selon l'ERD
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", unique = true, nullable = false)
    private String ticketNumber;

    // --- RELATIONS (Conformes à l'ERD) ---
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private MedicalService service;

    /**
     * The Doctor assigned to this ticket.
     * Maps to the doctor_id column in queue_tickets.
     */
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    // --- CHAMPS IA & TRIAGE ---
    @Column(name = "urgency_level")
    private String urgencyLevel;

    @Column(name = "has_appointment")
    private Boolean hasAppointment = false;

    @Column(name = "priority_score")
    private Integer priorityScore = 0;

    @Column(name = "position_number")
    private Integer positionNumber = 0;

    @Column(name = "estimated_waiting_time")
    private Integer estimatedWaitingTime = 0;

    // Nouveaux états: WAITING, CALLED, IN_CONSULTATION, COMPLETED, ABSENT
    private String status = "WAITING";

    // Notes de la consultation
    @Column(columnDefinition = "TEXT")
    private String notes;

    // --- CHAMPS NOTIFICATIONS ---
    @Column(name = "initial_email_sent")
    private Boolean initialEmailSent = false;

    @Column(name = "reminder_email_sent")
    private Boolean reminderEmailSent = false;

    // --- HORODATAGE (Lifecycle complet) ---
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "consultation_started_at")
    private LocalDateTime consultationStartedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}