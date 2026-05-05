package com.mediflow.entity;

public class Patient {
    private String fullName;
    private String email;
    private Integer age;

    // Constructeur vide pour Gson
    public Patient() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}