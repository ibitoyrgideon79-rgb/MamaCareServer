package com.Mamacare.Backend.MedicationRemiderPackage.Service;

import com.Mamacare.Backend.AuthenticationPackage.user.User;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.CreateMedicationRequest;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MarkMedicationTakenResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationHomeResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationTipResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.Entity.Medication;
import com.Mamacare.Backend.MedicationRemiderPackage.Entity.MedicationIntakeLog;
import com.Mamacare.Backend.MedicationRemiderPackage.Entity.MedicationReminder;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationFrequency;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationIntakeStatus;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderStatus;
import com.Mamacare.Backend.MedicationRemiderPackage.Repo.MedicationIntakeLogRepo;
import com.Mamacare.Backend.MedicationRemiderPackage.Repo.MedicationReminderRepo;
import com.Mamacare.Backend.MedicationRemiderPackage.Repo.MedicationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    private MedicationService medicationService;

    @Mock
    private MedicationRepo medicationRepository;

    @Mock
    private MedicationReminderRepo reminderRepository;

    @Mock
    private MedicationIntakeLogRepo intakeLogRepository;

    @Mock
    private MedicationTipService medicationTipService;

    @Mock
    private MedicationScheduleCalculator scheduleCalculator;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        medicationService = new MedicationService(
                medicationRepository,
                reminderRepository,
                intakeLogRepository,
                medicationTipService,
                scheduleCalculator
        );
    }

    @Test
    void createMedicationSavesMedicationAndCreatesNextReminder() {
        User user = user();
        CreateMedicationRequest request = CreateMedicationRequest.builder()
                .medicineName(" PregnaCare ")
                .dose(" 2 tablets ")
                .frequency(MedicationFrequency.DAILY)
                .medicationTime(LocalTime.of(17, 0))
                .startDate(LocalDate.of(2026, 5, 30))
                .timezone(null)
                .reminderEnabled(true)
                .reminderOffset(null)
                .notes(" After dinner ")
                .build();
        OffsetDateTime remindAt = OffsetDateTime.parse("2026-05-30T17:00:00+01:00");

        when(authentication.getPrincipal()).thenReturn(user);
        when(scheduleCalculator.normalizeTimezone(null)).thenReturn("Africa/Lagos");
        when(scheduleCalculator.calculateNextReminderAt(any(Medication.class))).thenReturn(Optional.of(remindAt));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> {
            Medication medication = invocation.getArgument(0);
            medication.setId(1L);
            return medication;
        });
        when(reminderRepository.findByMedicationIdOrderByRemindAtAsc(1L)).thenReturn(List.of());

        MedicationResponse response = medicationService.createMedication(request, authentication);

        ArgumentCaptor<Medication> medicationCaptor = ArgumentCaptor.forClass(Medication.class);
        ArgumentCaptor<MedicationReminder> reminderCaptor = ArgumentCaptor.forClass(MedicationReminder.class);
        verify(medicationRepository).save(medicationCaptor.capture());
        verify(reminderRepository).save(reminderCaptor.capture());

        Medication savedMedication = medicationCaptor.getValue();
        assertThat(savedMedication.getUser()).isEqualTo(user);
        assertThat(savedMedication.getMedicineName()).isEqualTo("PregnaCare");
        assertThat(savedMedication.getDose()).isEqualTo("2 tablets");
        assertThat(savedMedication.getTimezone()).isEqualTo("Africa/Lagos");
        assertThat(savedMedication.getReminderOffset()).isEqualTo(MedicationReminderOffset.ON_TIME);
        assertThat(savedMedication.getNotes()).isEqualTo("After dinner");
        assertThat(savedMedication.isActive()).isTrue();

        MedicationReminder savedReminder = reminderCaptor.getValue();
        assertThat(savedReminder.getMedication()).isSameAs(savedMedication);
        assertThat(savedReminder.getRemindAt()).isEqualTo(remindAt);
        assertThat(savedReminder.getStatus()).isEqualTo(MedicationReminderStatus.PENDING);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getMedicineName()).isEqualTo("PregnaCare");
        assertThat(response.getDisplayTime()).isEqualTo("5.00 PM");
        assertThat(response.getReminderText()).isEqualTo("We will remind you at 5.00 PM every day");
    }

    @Test
    void getTodayMedicationsFiltersScheduleAndMarksTakenState() {
        User user = user();
        LocalDate today = LocalDate.of(2026, 5, 30);
        Medication dueToday = medication(1L, MedicationFrequency.DAILY);
        Medication notDueToday = medication(2L, MedicationFrequency.WEEKLY);
        MedicationIntakeLog takenLog = MedicationIntakeLog.builder()
                .medication(dueToday)
                .intakeDate(today)
                .status(MedicationIntakeStatus.TAKEN)
                .build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(medicationRepository.findByUserIdAndActiveTrueOrderByMedicationTimeAsc(user.getId()))
                .thenReturn(List.of(dueToday, notDueToday));
        when(scheduleCalculator.shouldShowToday(dueToday)).thenReturn(true);
        when(scheduleCalculator.shouldShowToday(notDueToday)).thenReturn(false);
        when(scheduleCalculator.today("Africa/Lagos")).thenReturn(today);
        when(intakeLogRepository.findByMedicationIdInAndIntakeDate(List.of(1L), today))
                .thenReturn(List.of(takenLog));
        when(reminderRepository.findByMedicationIdOrderByRemindAtAsc(1L)).thenReturn(List.of());
        when(medicationTipService.getTips()).thenReturn(List.of(
                MedicationTipResponse.builder()
                        .title("Take with water")
                        .body("Water helps many medicines move through the body safely.")
                        .build()
        ));

        MedicationHomeResponse response = medicationService.getTodayMedications(authentication);

        assertThat(response.getTodayMedications()).hasSize(1);
        assertThat(response.getTodayMedications().get(0).getId()).isEqualTo(1L);
        assertThat(response.getTodayMedications().get(0).isTakenToday()).isTrue();
        assertThat(response.getTips()).hasSize(1);
        assertThat(response.getTips().get(0).getTitle()).isEqualTo("Take with water");
    }

    @Test
    void markTakenUpdatesExistingLogForToday() {
        User user = user();
        LocalDate today = LocalDate.of(2026, 5, 30);
        Medication medication = medication(1L, MedicationFrequency.DAILY);
        MedicationIntakeLog existingLog = MedicationIntakeLog.builder()
                .medication(medication)
                .intakeDate(today)
                .status(MedicationIntakeStatus.SKIPPED)
                .build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(medicationRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(medication));
        when(scheduleCalculator.today("Africa/Lagos")).thenReturn(today);
        when(intakeLogRepository.findByMedicationIdAndIntakeDate(1L, today)).thenReturn(Optional.of(existingLog));

        MarkMedicationTakenResponse response = medicationService.markTaken(1L, authentication);

        assertThat(response.getMedicationId()).isEqualTo(1L);
        assertThat(response.getIntakeDate()).isEqualTo(today);
        assertThat(response.isTakenToday()).isTrue();
        assertThat(existingLog.getStatus()).isEqualTo(MedicationIntakeStatus.TAKEN);
        assertThat(existingLog.getRecordedAt()).isNotNull();
        verify(intakeLogRepository).save(existingLog);
    }

    @Test
    void deactivateMedicationSoftDeletesAndCancelsPendingReminders() {
        User user = user();
        Medication medication = medication(1L, MedicationFrequency.DAILY);
        MedicationReminder pendingReminder = MedicationReminder.builder()
                .id(10L)
                .medication(medication)
                .reminderOffset(MedicationReminderOffset.ON_TIME)
                .remindAt(OffsetDateTime.parse("2026-05-30T17:00:00+01:00"))
                .status(MedicationReminderStatus.PENDING)
                .build();
        MedicationReminder sentReminder = MedicationReminder.builder()
                .id(11L)
                .medication(medication)
                .reminderOffset(MedicationReminderOffset.ON_TIME)
                .remindAt(OffsetDateTime.parse("2026-05-29T17:00:00+01:00"))
                .status(MedicationReminderStatus.SENT)
                .build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(medicationRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(medication));
        when(reminderRepository.findByMedicationIdOrderByRemindAtAsc(1L))
                .thenReturn(List.of(pendingReminder, sentReminder));

        medicationService.deactivateMedication(1L, authentication);

        assertThat(medication.isActive()).isFalse();
        assertThat(pendingReminder.getStatus()).isEqualTo(MedicationReminderStatus.CANCELLED);
        assertThat(sentReminder.getStatus()).isEqualTo(MedicationReminderStatus.SENT);
        verify(medicationRepository).save(medication);
        verify(reminderRepository).saveAll(List.of(pendingReminder, sentReminder));
    }

    private User user() {
        return User.builder()
                .id(7)
                .email("mama@example.com")
                .build();
    }

    private Medication medication(Long id, MedicationFrequency frequency) {
        return Medication.builder()
                .id(id)
                .user(user())
                .medicineName("PregnaCare")
                .dose("2 tablets")
                .frequency(frequency)
                .medicationTime(LocalTime.of(17, 0))
                .startDate(LocalDate.of(2026, 5, 30))
                .timezone("Africa/Lagos")
                .reminderEnabled(true)
                .reminderOffset(MedicationReminderOffset.ON_TIME)
                .active(true)
                .build();
    }
}
