package com.mediflow.service;

import com.mediflow.entity.Doctor;
import com.mediflow.entity.MedicalService;
import com.mediflow.entity.User;
import com.mediflow.repository.DoctorRepository;
import com.mediflow.repository.MedicalServiceRepository;
import com.mediflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing Doctor business logic.
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository, MedicalServiceRepository medicalServiceRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.medicalServiceRepository = medicalServiceRepository;
    }

    /**
     * Retrieves all doctors in the system.
     * @return List of all doctors.
     */
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Retrieves doctors associated with a specific medical service.
     * @param serviceId ID of the medical service.
     * @return List of doctors in the specified service.
     */
    public List<Doctor> getDoctorsByService(Long serviceId) {
        return doctorRepository.findByServiceId(serviceId);
    }

    /**
     * Registers a new doctor by creating a User account and linking it to a MedicalService.
     * @param fullName Doctor's full name.
     * @param email Doctor's email.
     * @param password Password (plain text as per current system design).
     * @param serviceId The ID of the MedicalService to assign the doctor to.
     * @return The newly created Doctor entity.
     */
    @Transactional
    public Doctor registerDoctor(String fullName, String email, String password, Long serviceId) {
        // 1. Create the User account
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password); // Note: For a production app, use BCrypt
        user.setRole("DOCTOR");
        user.setEnabled(true);
        User savedUser = userRepository.save(user);

        // 2. Fetch the assigned Medical Service
        MedicalService service = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Medical Service not found with ID: " + serviceId));

        // 3. Create and save the Doctor entity
        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setService(service);
        
        return doctorRepository.save(doctor);
    }
}
