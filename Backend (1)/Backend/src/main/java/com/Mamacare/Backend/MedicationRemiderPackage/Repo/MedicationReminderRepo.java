package com.Mamacare.Backend.MedicationRemiderPackage.Repo;

import com.Mamacare.Backend.MedicationRemiderPackage.Entity.MedicationReminder;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;

public interface MedicationReminderRepo extends JpaRepository<MedicationReminder, Long> {

    List<MedicationReminder> findByMedicationIdOrderByRemindAtAsc(Long medicationId);

    List<MedicationReminder> findByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(
            MedicationReminderStatus status,
            OffsetDateTime now
    );
}