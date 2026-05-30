package com.Mamacare.Backend.MedicationRemiderPackage.Service;

import com.Mamacare.Backend.AuthenticationPackage.user.User;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.CreateMedicationRequest;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MarkMedicationTakenResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationHomeResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationReminderResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.Entity.Medication;
import com.Mamacare.Backend.MedicationRemiderPackage.Entity.MedicationIntakeLog;
import com.Mamacare.Backend.MedicationRemiderPackage.Entity.MedicationReminder;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationIntakeStatus;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderStatus;
import com.Mamacare.Backend.MedicationRemiderPackage.Service.MedicationTipService;
import com.Mamacare.Backend.MedicationRemiderPackage.Repo.MedicationIntakeLogRepo;
import com.Mamacare.Backend.MedicationRemiderPackage.Repo.MedicationReminderRepo;
import com.Mamacare.Backend.MedicationRemiderPackage.Repo.MedicationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("h.mm a", Locale.ENGLISH);

    private final MedicationRepo medicationRepository;
    private final MedicationReminderRepo reminderRepository;
    private final MedicationIntakeLogRepo intakeLogRepository;
    private final MedicationTipService medicationTipService;
    private final MedicationScheduleCalculator scheduleCalculator;

    @Transactional
    public MedicationResponse createMedication(CreateMedicationRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        validateCreateRequest(request);

        String timezone = scheduleCalculator.normalizeTimezone(request.getTimezone());
        LocalDate startDate = request.getStartDate() == null
                ? scheduleCalculator.today(timezone)
                : request.getStartDate();
        MedicationReminderOffset reminderOffset = request.getReminderOffset() == null
                ? MedicationReminderOffset.ON_TIME
                : request.getReminderOffset();

        Medication medication = Medication.builder()
                .user(currentUser)
                .medicineName(request.getMedicineName().trim())
                .dose(request.getDose().trim())
                .frequency(request.getFrequency())
                .medicationTime(request.getMedicationTime())
                .startDate(startDate)
                .timezone(timezone)
                .reminderEnabled(request.isReminderEnabled())
                .reminderOffset(reminderOffset)
                .notes(normalizeText(request.getNotes()))
                .active(true)
                .build();

        Medication savedMedication = medicationRepository.save(medication);
        createNextReminderIfNeeded(savedMedication);

        return toResponse(savedMedication, Set.of());
    }

    @Transactional(readOnly = true)
    public MedicationHomeResponse getTodayMedications(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<Medication> activeMedications = medicationRepository
                .findByUserIdAndActiveTrueOrderByMedicationTimeAsc(currentUser.getId());
        List<Medication> todayMedications = activeMedications.stream()
                .filter(scheduleCalculator::shouldShowToday)
                .toList();
        Set<Long> takenMedicationIds = loadTakenMedicationIdsForToday(todayMedications);

        List<MedicationResponse> medicationResponses = todayMedications.stream()
                .map(medication -> toResponse(medication, takenMedicationIds))
                .toList();

        return MedicationHomeResponse.builder()
                .todayMedications(medicationResponses)
                .tips(medicationTipService.getTips())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MedicationResponse> getAllMedications(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<Medication> medications = medicationRepository
                .findByUserIdAndActiveTrueOrderByMedicationTimeAsc(currentUser.getId());
        Set<Long> takenMedicationIds = loadTakenMedicationIdsForToday(medications);

        return medications.stream()
                .map(medication -> toResponse(medication, takenMedicationIds))
                .toList();
    }

    @Transactional
    public MarkMedicationTakenResponse markTaken(Long medicationId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Medication medication = findMedicationOwnedByUser(medicationId, currentUser.getId());

        if (!medication.isActive()) {
            throw new IllegalArgumentException("Medication is inactive.");
        }

        LocalDate intakeDate = scheduleCalculator.today(medication.getTimezone());
        MedicationIntakeLog intakeLog = intakeLogRepository
                .findByMedicationIdAndIntakeDate(medication.getId(), intakeDate)
                .orElseGet(() -> MedicationIntakeLog.builder()
                        .medication(medication)
                        .intakeDate(intakeDate)
                        .build());

        intakeLog.setStatus(MedicationIntakeStatus.TAKEN);
        intakeLog.setRecordedAt(OffsetDateTime.now(ZoneId.of(medication.getTimezone())));
        intakeLogRepository.save(intakeLog);

        return MarkMedicationTakenResponse.builder()
                .medicationId(medication.getId())
                .intakeDate(intakeDate)
                .takenToday(true)
                .build();
    }

    @Transactional
    public void deactivateMedication(Long medicationId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Medication medication = findMedicationOwnedByUser(medicationId, currentUser.getId());

        medication.setActive(false);
        medicationRepository.save(medication);

        List<MedicationReminder> reminders = reminderRepository.findByMedicationIdOrderByRemindAtAsc(medication.getId());
        for (MedicationReminder reminder : reminders) {
            if (reminder.getStatus() == MedicationReminderStatus.PENDING) {
                reminder.setStatus(MedicationReminderStatus.CANCELLED);
            }
        }
        reminderRepository.saveAll(reminders);
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }

        return currentUser;
    }

    private Medication findMedicationOwnedByUser(Long medicationId, Integer userId) {
        return medicationRepository.findByIdAndUserId(medicationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found."));
    }

    private void validateCreateRequest(CreateMedicationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Medication request is required.");
        }
        if (request.getMedicineName() == null || request.getMedicineName().isBlank()) {
            throw new IllegalArgumentException("Medicine name is required.");
        }
        if (request.getMedicineName().length() > 120) {
            throw new IllegalArgumentException("Medicine name must not exceed 120 characters.");
        }
        if (request.getDose() == null || request.getDose().isBlank()) {
            throw new IllegalArgumentException("Dose is required.");
        }
        if (request.getDose().length() > 80) {
            throw new IllegalArgumentException("Dose must not exceed 80 characters.");
        }
        if (request.getFrequency() == null) {
            throw new IllegalArgumentException("Frequency is required.");
        }
        if (request.getMedicationTime() == null) {
            throw new IllegalArgumentException("Medication time is required.");
        }
        if (request.getNotes() != null && request.getNotes().length() > 1000) {
            throw new IllegalArgumentException("Notes must not exceed 1000 characters.");
        }
    }

    private void createNextReminderIfNeeded(Medication medication) {
        scheduleCalculator.calculateNextReminderAt(medication)
                .ifPresent(remindAt -> {
                    MedicationReminder reminder = MedicationReminder.builder()
                            .medication(medication)
                            .reminderOffset(medication.getReminderOffset())
                            .remindAt(remindAt)
                            .status(MedicationReminderStatus.PENDING)
                            .build();

                    reminderRepository.save(reminder);
                });
    }

    private Set<Long> loadTakenMedicationIdsForToday(List<Medication> medications) {
        if (medications.isEmpty()) {
            return Set.of();
        }

        Map<LocalDate, List<Long>> medicationIdsByDate = medications.stream()
                .collect(Collectors.groupingBy(
                        medication -> scheduleCalculator.today(medication.getTimezone()),
                        Collectors.mapping(Medication::getId, Collectors.toList())
                ));

        Set<Long> takenMedicationIds = new HashSet<>();

        for (Map.Entry<LocalDate, List<Long>> entry : medicationIdsByDate.entrySet()) {
            List<MedicationIntakeLog> logs = intakeLogRepository.findByMedicationIdInAndIntakeDate(
                    entry.getValue(),
                    entry.getKey()
            );

            for (MedicationIntakeLog log : logs) {
                if (log.getStatus() == MedicationIntakeStatus.TAKEN) {
                    takenMedicationIds.add(log.getMedication().getId());
                }
            }
        }

        return takenMedicationIds;
    }

    private MedicationResponse toResponse(Medication medication, Set<Long> takenMedicationIds) {
        List<MedicationReminderResponse> reminders = reminderRepository
                .findByMedicationIdOrderByRemindAtAsc(medication.getId())
                .stream()
                .map(reminder -> MedicationReminderResponse.builder()
                        .offset(reminder.getReminderOffset())
                        .remindAt(reminder.getRemindAt())
                        .status(reminder.getStatus())
                        .build())
                .toList();

        return MedicationResponse.builder()
                .id(medication.getId())
                .medicineName(medication.getMedicineName())
                .dose(medication.getDose())
                .frequency(medication.getFrequency())
                .medicationTime(medication.getMedicationTime())
                .displayTime(DISPLAY_TIME_FORMATTER.format(medication.getMedicationTime()))
                .startDate(medication.getStartDate())
                .timezone(medication.getTimezone())
                .notes(medication.getNotes())
                .reminderEnabled(medication.isReminderEnabled())
                .reminderText(toReminderText(medication))
                .takenToday(takenMedicationIds.contains(medication.getId()))
                .reminders(reminders)
                .build();
    }

    private String toReminderText(Medication medication) {
        if (!medication.isReminderEnabled()) {
            return "Reminder is off";
        }

        String displayTime = DISPLAY_TIME_FORMATTER.format(medication.getMedicationTime());
        String frequencyText = switch (medication.getFrequency()) {
            case DAILY -> "every day";
            case WEEKLY -> "every week";
            case AS_NEEDED -> "on the selected day";
        };

        return switch (medication.getReminderOffset()) {
            case ON_TIME -> "We will remind you at " + displayTime + " " + frequencyText;
            case FIFTEEN_MINUTES_BEFORE -> "We will remind you 15 minutes before " + displayTime + " " + frequencyText;
            case THIRTY_MINUTES_BEFORE -> "We will remind you 30 minutes before " + displayTime + " " + frequencyText;
        };
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
