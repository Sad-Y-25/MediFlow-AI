package com.mediflow.entity;

public class Ticket {
    private Long id;
    private String ticketNumber;

    private Patient patient; // L'objet imbriqué
    private String urgencyLevel;
    private Boolean hasAppointment;
    private Integer priorityScore;
    private Integer estimatedWaitingTime;
    private String status;
    private String createdAt;
    private String completedAt;

    public Ticket() {}

    // --- GETTERS POUR JAVAFX (PropertyValueFactory) ---
    public Long getId() { return id; }

    // Astuce : JavaFX appellera cette méthode si tu lui demandes "patientName"
    public String getPatientName() {
        return patient != null ? patient.getFullName() : "Inconnu";
    }

    public String getTicketNumber() { return ticketNumber; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public Integer getPriorityScore() { return priorityScore; }
    public Integer getEstimatedWaitingTime() { return estimatedWaitingTime; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getCompletedAt() { return completedAt; }
    public Patient getPatient() { return patient; }
    public Boolean getHasAppointment() { return hasAppointment; }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public void setHasAppointment(Boolean hasAppointment) { this.hasAppointment = hasAppointment; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }
    public void setEstimatedWaitingTime(Integer estimatedWaitingTime) { this.estimatedWaitingTime = estimatedWaitingTime; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
}