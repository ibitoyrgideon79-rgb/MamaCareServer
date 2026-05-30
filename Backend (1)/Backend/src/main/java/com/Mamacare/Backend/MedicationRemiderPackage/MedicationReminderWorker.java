package com.Mamacare.Backend.MedicationRemiderPackage;

import com.Mamacare.Backend.MedicationRemiderPackage.Entity.Medication;
import com.Mamacare.Backend.MedicationRemiderPackage.Entity.MedicationReminder;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderStatus;
import com.Mamacare.Backend.MedicationRemiderPackage.Repo.MedicationReminderRepo;
import com.Mamacare.Backend.MedicationRemiderPackage.Service.MedicationScheduleCalculator;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationReminderWorker {

    private final MedicationReminderRepo reminderRepository;
    private final MedicationScheduleCalculator scheduleCalculator;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processDueReminders() {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of(MedicationScheduleCalculator.DEFAULT_TIMEZONE));
        List<MedicationReminder> dueReminders = reminderRepository
                .findByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(MedicationReminderStatus.PENDING, now);

        for (MedicationReminder reminder : dueReminders) {
            Medication medication = reminder.getMedication();

            reminder.setStatus(MedicationReminderStatus.SENT);
            reminderRepository.save(reminder);

            queueNextReminderIfNeeded(medication);
        }
    }

    private void queueNextReminderIfNeeded(Medication medication) {
        scheduleCalculator.calculateNextReminderAt(medication)
                .ifPresent(nextReminderAt -> {
                    MedicationReminder nextReminder = MedicationReminder.builder()
                            .medication(medication)
                            .reminderOffset(medication.getReminderOffset())
                            .remindAt(nextReminderAt)
                            .status(MedicationReminderStatus.PENDING)
                            .build();

                    reminderRepository.save(nextReminder);
                });
    }
}
