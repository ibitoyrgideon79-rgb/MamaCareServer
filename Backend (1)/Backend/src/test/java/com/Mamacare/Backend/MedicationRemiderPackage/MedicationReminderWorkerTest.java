package com.Mamacare.Backend.MedicationRemiderPackage;

import com.Mamacare.Backend.MedicationRemiderPackage.Entity.Medication;
import com.Mamacare.Backend.MedicationRemiderPackage.Entity.MedicationReminder;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationFrequency;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderStatus;
import com.Mamacare.Backend.MedicationRemiderPackage.Repo.MedicationReminderRepo;
import com.Mamacare.Backend.MedicationRemiderPackage.Service.MedicationScheduleCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationReminderWorkerTest {

    @Mock
    private MedicationReminderRepo reminderRepository;

    @Mock
    private MedicationScheduleCalculator scheduleCalculator;

    @Test
    void processDueRemindersMarksReminderSentAndQueuesNextReminder() {
        Medication medication = Medication.builder()
                .id(1L)
                .medicineName("PregnaCare")
                .dose("2 tablets")
                .frequency(MedicationFrequency.DAILY)
                .medicationTime(LocalTime.of(17, 0))
                .startDate(LocalDate.of(2026, 5, 30))
                .timezone("Africa/Lagos")
                .reminderEnabled(true)
                .reminderOffset(MedicationReminderOffset.ON_TIME)
                .active(true)
                .build();
        MedicationReminder dueReminder = MedicationReminder.builder()
                .id(10L)
                .medication(medication)
                .reminderOffset(MedicationReminderOffset.ON_TIME)
                .remindAt(OffsetDateTime.parse("2026-05-30T17:00:00+01:00"))
                .status(MedicationReminderStatus.PENDING)
                .build();
        OffsetDateTime nextReminderAt = OffsetDateTime.parse("2026-05-31T17:00:00+01:00");

        when(reminderRepository.findByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(
                eq(MedicationReminderStatus.PENDING),
                any(OffsetDateTime.class)
        )).thenReturn(List.of(dueReminder));
        when(scheduleCalculator.calculateNextReminderAt(medication)).thenReturn(Optional.of(nextReminderAt));

        MedicationReminderWorker worker = new MedicationReminderWorker(reminderRepository, scheduleCalculator);
        worker.processDueReminders();

        ArgumentCaptor<MedicationReminder> reminderCaptor = ArgumentCaptor.forClass(MedicationReminder.class);
        verify(reminderRepository, times(2)).save(reminderCaptor.capture());

        List<MedicationReminder> savedReminders = reminderCaptor.getAllValues();
        assertThat(savedReminders.get(0).getStatus()).isEqualTo(MedicationReminderStatus.SENT);
        assertThat(savedReminders.get(1).getMedication()).isSameAs(medication);
        assertThat(savedReminders.get(1).getRemindAt()).isEqualTo(nextReminderAt);
        assertThat(savedReminders.get(1).getStatus()).isEqualTo(MedicationReminderStatus.PENDING);
    }
}
