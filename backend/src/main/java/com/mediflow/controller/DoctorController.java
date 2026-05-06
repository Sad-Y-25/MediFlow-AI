package com.mediflow.controller;

import com.mediflow.entity.Doctor;
import com.mediflow.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing Doctor-related endpoints.
 */
@RestController
@RequestMapping("/api/doctors")
@CrossOrigin("*")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /**
     * Fetch all doctors.
     * @return List of all doctors.
     */
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    /**
     * Fetch all doctors for a specific medical service.
     * @param serviceId ID of the medical service.
     * @return List of doctors in that service.
     */
    @GetMapping("/service/{serviceId}")
    public List<Doctor> getDoctorsByService(@PathVariable Long serviceId) {
        return doctorService.getDoctorsByService(serviceId);
    }

    /**
     * Request body representation for creating a new doctor.
     */
    public static class DoctorRegistrationRequest {
        public String fullName;
        public String email;
        public String password;
        public Long serviceId;
    }

    /**
     * Register a new doctor.
     * @param request Data transfer object containing doctor registration details.
     * @return The created Doctor entity.
     */
    @PostMapping("/register")
    public ResponseEntity<Doctor> registerDoctor(@RequestBody DoctorRegistrationRequest request) {
        try {
            Doctor newDoctor = doctorService.registerDoctor(
                    request.fullName,
                    request.email,
                    request.password,
                    request.serviceId
            );
            return ResponseEntity.ok(newDoctor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
