package com.mediflow.entity;

public class MedicalService {
    private Long id;
    private String name;
    private Integer averageConsultationTime;
    private Boolean active;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAverageConsultationTime() { return averageConsultationTime; }
    public void setAverageConsultationTime(Integer averageConsultationTime) { this.averageConsultationTime = averageConsultationTime; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
