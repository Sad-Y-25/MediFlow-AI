package com.mediflow.entity;

public class Ticket {
    private Long id;
    private String patientName;
    private String reason;
    private int urgencyLevel;
    private String status;
    private String createdAt;
    private String completedAt;

    // Constructeur vide pour Gson
    public Ticket() {}

    // --- GETTERS (Indispensables pour le TableView) ---

    public Long getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public int getUrgencyLevel() {
        return urgencyLevel;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }
    // --- SETTERS ---

    public void setId(Long id) { this.id = id; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setUrgencyLevel(int urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public void setReason(String reason) { this.reason = reason; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
}