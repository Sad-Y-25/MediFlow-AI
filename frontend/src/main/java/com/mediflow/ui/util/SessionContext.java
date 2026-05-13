package com.mediflow.ui.util;

public class SessionContext {
    private static SessionContext instance;
    
    private Long userId;
    private String fullName;
    private String role;
    private Long doctorId; // Null if not a doctor
    
    private SessionContext() {}
    
    public static SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }
    
    public void clear() {
        userId = null;
        fullName = null;
        role = null;
        doctorId = null;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
}
