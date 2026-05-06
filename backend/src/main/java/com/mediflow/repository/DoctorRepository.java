package com.mediflow.repository;

import com.mediflow.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Doctor entities.
 * Inherits standard CRUD operations from JpaRepository.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Finds all doctors assigned to a specific medical service.
     * @param serviceId The ID of the medical service.
     * @return A list of doctors working in the specified service.
     */
    List<Doctor> findByServiceId(Long serviceId);

    /**
     * Finds a doctor by their associated user account ID.
     * @param userId The ID of the user account.
     * @return An Optional containing the doctor if found.
     */
    Optional<Doctor> findByUserId(Long userId);
}
