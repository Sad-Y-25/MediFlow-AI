package com.mediflow.entity;

public class Doctor {
    private Long id;
    private User user;
    private MedicalService service;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public MedicalService getService() { return service; }
    public void setService(MedicalService service) { this.service = service; }
}
