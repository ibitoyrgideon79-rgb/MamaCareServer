package com.Mamacare.Backend.MedicationRemiderPackage.Repo;

import com.Mamacare.Backend.MedicationRemiderPackage.Entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicationRepo extends JpaRepository<Medication, Long> {

    List<Medication> findByUserIdAndActiveTrueOrderByMedicationTimeAsc(Integer userId);

    Optional<Medication> findByIdAndUserId(Long id, Integer userId);

    List<Medication> findByUserIdAndActiveTrueAndStartDateLessThanEqualOrderByMedicationTimeAsc(
            Integer userId,
            LocalDate today
    );
}