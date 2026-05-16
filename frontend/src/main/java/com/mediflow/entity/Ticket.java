package com.mediflow.entity;

public class Ticket {
    private Long id;
    private String ticketNumber;
    private Patient patient;
    private String urgencyLevel;
    private Boolean hasAppointment;
    private Integer priorityScore;
    private Integer estimatedWaitingTime;
    private Integer positionNumber; // Ajouté pour la salle d'attente
    private String status;
    private String createdAt;
    private String completedAt;

    // Relations ajoutées pour l'affichage par médecin
    private Doctor doctor;
    private MedicalService service;

    public Ticket() {}

    // --- GETTERS POUR JAVAFX (PropertyValueFactory) ---
    public Long getId() { return id; }

    public String getTicketNumber() { return ticketNumber; }

    public String getUrgencyLevel() { return urgencyLevel; }

    public Integer getPriorityScore() { return priorityScore; }

    public Integer getEstimatedWaitingTime() { return estimatedWaitingTime; }

    public String getStatus() { return status; }

    public String getCreatedAt() { return createdAt; }

    public String getCompletedAt() { return completedAt; }

    public Patient getPatient() { return patient; }

    public Boolean getHasAppointment() { return hasAppointment; }

    // CRUCIAL : Doit être parfaitement orthographié en camelCase et PUBLIC
    public Integer getPositionNumber() {
        return positionNumber;
    }

    // --- GETTERS INTELLIGENTS POUR LES SOUSTABLES ---
    public String getPatientName() {
        return patient != null ? patient.getFullName() : "Inconnu";
    }

    public String getDoctorName() {
        if (doctor != null && doctor.getUser() != null) {
            return "Dr. " + doctor.getUser().getFullName();
        }
        return "En attente d'affectation";
    }

    public String getServiceName() {
        if (doctor != null && doctor.getService() != null) {
            return doctor.getService().getName();
        }
        return "Triage général";
    }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public void setHasAppointment(Boolean hasAppointment) { this.hasAppointment = hasAppointment; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }
    public void setEstimatedWaitingTime(Integer estimatedWaitingTime) { this.estimatedWaitingTime = estimatedWaitingTime; }
    public void setPositionNumber(Integer positionNumber) { this.positionNumber = positionNumber; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public MedicalService getService() { return service; }
    public void setService(MedicalService service) { this.service = service; }
}