package com.Mamacare.Backend.MedicationRemiderPackage.Repo;

import com.Mamacare.Backend.MedicationRemiderPackage.Entity.MedicationIntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicationIntakeLogRepo extends JpaRepository<MedicationIntakeLog, Long> {

    Optional<MedicationIntakeLog> findByMedicationIdAndIntakeDate(Long medicationId, LocalDate intakeDate);

    List<MedicationIntakeLog> findByMedicationIdInAndIntakeDate(List<Long> medicationIds, LocalDate intakeDate);
}