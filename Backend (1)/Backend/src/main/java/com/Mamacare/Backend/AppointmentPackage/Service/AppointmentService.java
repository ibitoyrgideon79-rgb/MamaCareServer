package com.Mamacare.Backend.AppointmentPackage.Service;

import com.Mamacare.Backend.AppointmentPackage.Dtos.AppointmentResponse;
import com.Mamacare.Backend.AppointmentPackage.Dtos.AppointmentChecklistItemResponse;
import com.Mamacare.Backend.AppointmentPackage.Dtos.AppointmentReminderResponse;
import com.Mamacare.Backend.AppointmentPackage.Dtos.CreateAppointmentRequest;
import com.Mamacare.Backend.AppointmentPackage.Dtos.NextAppointmentResponse;
import com.Mamacare.Backend.AppointmentPackage.Dtos.UpdateChecklistItemRequest;
import com.Mamacare.Backend.AppointmentPackage.Entity.Appointment;
import com.Mamacare.Backend.AppointmentPackage.Entity.AppointmentChecklistItem;
import com.Mamacare.Backend.AppointmentPackage.Entity.AppointmentReminder;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentReminderOffset;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentReminderStatus;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentStatus;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentType;
import com.Mamacare.Backend.AppointmentPackage.Repo.AppointmentChecklistItemRepo;
import com.Mamacare.Backend.AppointmentPackage.Repo.AppointmentRepo;
import com.Mamacare.Backend.AppointmentPackage.Repo.AppointmentReminderRepo;
import com.Mamacare.Backend.AuthenticationPackage.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final String DEFAULT_TIMEZONE = "Africa/Lagos";
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh.mm a");

    private final AppointmentRepo appointmentRepository;
    private final AppointmentReminderRepo reminderRepository;
    private final AppointmentChecklistItemRepo checklistItemRepository;

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        validateCreateRequest(request);

        String timezone = normalizeTimezone(request.getTimezone());
        OffsetDateTime scheduledStartAt = toOffsetDateTime(
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                timezone
        );

        if (scheduledStartAt.isBefore(OffsetDateTime.now(ZoneId.of(timezone)))) {
            throw new IllegalArgumentException("Appointment date and time must be in the future.");
        }

        Appointment appointment = Appointment.builder()
                .user(currentUser)
                .appointmentType(request.getAppointmentType())
                .status(AppointmentStatus.SCHEDULED)
                .scheduledStartAt(scheduledStartAt)
                .timezone(timezone)
                .locationName(request.getLocationName().trim())
                .notes(normalizeText(request.getNotes()))
                .reminderEnabled(request.isReminderEnabled())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        if (request.isReminderEnabled()) {
            createReminderRows(savedAppointment, request.getReminderOffsets());
        }

        createDefaultChecklist(savedAppointment);

        return toResponse(savedAppointment);
    }

    @Transactional
    public AppointmentChecklistItemResponse updateChecklistItem(
            Long appointmentId,
            Long itemId,
            UpdateChecklistItemRequest request,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);

        Appointment appointment = appointmentRepository
                .findByIdAndUserId(appointmentId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        AppointmentChecklistItem item = checklistItemRepository
                .findByIdAndAppointmentId(itemId, appointment.getId())
                .orElseThrow(() -> new IllegalArgumentException("Checklist item not found."));

        item.setCompleted(request.isCompleted());
        AppointmentChecklistItem savedItem = checklistItemRepository.save(item);

        return toChecklistItemResponse(savedItem);
    }

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    private void validateCreateRequest(CreateAppointmentRequest request) {
        if (request.getAppointmentType() == null) {
            throw new IllegalArgumentException("Appointment type is required.");
        }
        if (request.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Appointment date is required.");
        }
        if (request.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Appointment time is required.");
        }
        if (request.getLocationName() == null || request.getLocationName().isBlank()) {
            throw new IllegalArgumentException("Location is required.");
        }
        if (request.getLocationName().length() > 180) {
            throw new IllegalArgumentException("Location must not exceed 180 characters.");
        }
        if (request.getNotes() != null && request.getNotes().length() > 1000) {
            throw new IllegalArgumentException("Notes must not exceed 1000 characters.");
        }
    }

    private String normalizeTimezone(String timezone) {
        String normalizedTimezone = (timezone == null || timezone.isBlank()) ? DEFAULT_TIMEZONE : timezone.trim();
        ZoneId.of(normalizedTimezone);
        return normalizedTimezone;
    }

    private OffsetDateTime toOffsetDateTime(LocalDate date, LocalTime time, String timezone) {
        return date.atTime(time)
                .atZone(ZoneId.of(timezone))
                .toOffsetDateTime();
    }

    private void createReminderRows(Appointment appointment, List<AppointmentReminderOffset> reminderOffsets) {
        if (reminderOffsets == null || reminderOffsets.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneId.of(appointment.getTimezone()));

        for (AppointmentReminderOffset offset : reminderOffsets) {
            OffsetDateTime remindAt = calculateRemindAt(appointment.getScheduledStartAt(), offset);

            if (remindAt.isBefore(now)) {
                continue;
            }

            AppointmentReminder reminder = AppointmentReminder.builder()
                    .appointment(appointment)
                    .reminderOffset(offset)
                    .remindAt(remindAt)
                    .status(AppointmentReminderStatus.PENDING)
                    .build();

            reminderRepository.save(reminder);
        }
    }

    private OffsetDateTime calculateRemindAt(OffsetDateTime scheduledStartAt, AppointmentReminderOffset offset) {
        return switch (offset) {
            case ONE_DAY_BEFORE -> scheduledStartAt.minusDays(1);
            case SIX_HOURS_BEFORE -> scheduledStartAt.minusHours(6);
            case ONE_HOUR_BEFORE -> scheduledStartAt.minusHours(1);
        };
    }

    private void createDefaultChecklist(Appointment appointment) {
        List<String> items = defaultChecklistFor(appointment.getAppointmentType());

        for (int i = 0; i < items.size(); i++) {
            AppointmentChecklistItem item = AppointmentChecklistItem.builder()
                    .appointment(appointment)
                    .text(items.get(i))
                    .completed(false)
                    .sortOrder(i + 1)
                    .build();

            checklistItemRepository.save(item);
        }
    }

    private List<String> defaultChecklistFor(AppointmentType appointmentType) {
        return switch (appointmentType) {
            case ANTENATAL -> List.of(
                    "Prepare questions to ask your doctor.",
                    "Take your hospital card.",
                    "Come with previous test result (If there's any)."
            );
            case ULTRASOUND -> List.of(
                    "Take your hospital card.",
                    "Confirm if you need to drink water before the scan.",
                    "Come with previous scan result (If there's any)."
            );
            case LAB_TEST -> List.of(
                    "Take your hospital card.",
                    "Confirm if fasting is required.",
                    "Come with previous test result (If there's any)."
            );
            default -> List.of(
                    "Prepare questions to ask your doctor.",
                    "Take your hospital card.",
                    "Come with previous test result (If there's any)."
            );
        };
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        LocalDate appointmentDate = appointment.getScheduledStartAt().toLocalDate();
        LocalTime appointmentTime = appointment.getScheduledStartAt().toLocalTime();
        List<AppointmentReminderResponse> reminders = reminderRepository
                .findByAppointmentIdOrderByRemindAtAsc(appointment.getId())
                .stream()
                .map(reminder -> AppointmentReminderResponse.builder()
                        .offset(reminder.getReminderOffset())
                        .remindAt(reminder.getRemindAt())
                        .status(reminder.getStatus())
                        .build())
                .toList();
        List<AppointmentChecklistItemResponse> checklist = checklistItemRepository
                .findByAppointmentIdOrderBySortOrderAsc(appointment.getId())
                .stream()
                .map(this::toChecklistItemResponse)
                .toList();

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .appointmentType(appointment.getAppointmentType())
                .appointmentTypeLabel(toAppointmentTypeLabel(appointment.getAppointmentType()))
                .status(appointment.getStatus())
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .displayTime(DISPLAY_TIME_FORMATTER.format(appointmentTime))
                .locationName(appointment.getLocationName())
                .notes(appointment.getNotes())
                .daysToGo(daysToGo(appointment))
                .reminderEnabled(appointment.isReminderEnabled())
                .reminders(reminders)
                .checklist(checklist)
                .build();
    }

    private AppointmentChecklistItemResponse toChecklistItemResponse(AppointmentChecklistItem item) {
        return AppointmentChecklistItemResponse.builder()
                .id(item.getId())
                .text(item.getText())
                .completed(item.isCompleted())
                .build();
    }

    private String toAppointmentTypeLabel(AppointmentType appointmentType) {
        return switch (appointmentType) {
            case ANTENATAL -> "Antenatal Appointment";
            case ULTRASOUND -> "Ultrasound Appointment";
            case LAB_TEST -> "Lab Test Appointment";
            case DOCTOR_CONSULTATION -> "Doctor Consultation";
            case VACCINATION -> "Vaccination Appointment";
            case OTHER -> "Appointment";
        };
    }

    private long daysToGo(Appointment appointment) {
        LocalDate today = LocalDate.now(ZoneId.of(appointment.getTimezone()));
        LocalDate appointmentDate = appointment.getScheduledStartAt().toLocalDate();
        return Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(today, appointmentDate));
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional(readOnly = true)
    public NextAppointmentResponse getNextAppointment(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of(DEFAULT_TIMEZONE));

        return appointmentRepository
                .findFirstByUserIdAndStatusAndScheduledStartAtGreaterThanEqualOrderByScheduledStartAtAsc(
                        currentUser.getId(),
                        AppointmentStatus.SCHEDULED,
                        now
                )
                .map(appointment -> NextAppointmentResponse.builder()
                        .hasUpcomingAppointment(true)
                        .appointment(toResponse(appointment))
                        .build())
                .orElseGet(() -> NextAppointmentResponse.builder()
                        .hasUpcomingAppointment(false)
                        .appointment(null)
                        .build());
    }
}
