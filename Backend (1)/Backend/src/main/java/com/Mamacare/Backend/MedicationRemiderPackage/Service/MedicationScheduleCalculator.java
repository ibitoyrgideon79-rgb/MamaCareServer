package com.Mamacare.Backend.MedicationRemiderPackage.Service;

import com.Mamacare.Backend.MedicationRemiderPackage.Entity.Medication;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationFrequency;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class MedicationScheduleCalculator {

    public static final String DEFAULT_TIMEZONE = "Africa/Lagos";

    public String normalizeTimezone(String timezone) {
        String normalizedTimezone = (timezone == null || timezone.isBlank())
                ? DEFAULT_TIMEZONE
                : timezone.trim();

        ZoneId.of(normalizedTimezone);
        return normalizedTimezone;
    }

    public LocalDate today(String timezone) {
        return LocalDate.now(ZoneId.of(normalizeTimezone(timezone)));
    }

    public boolean shouldShowToday(Medication medication) {
        return shouldOccurOn(medication, today(medication.getTimezone()));
    }

    public boolean shouldOccurOn(Medication medication, LocalDate date) {
        if (date.isBefore(medication.getStartDate())) {
            return false;
        }

        if (medication.getFrequency() == MedicationFrequency.DAILY) {
            return true;
        }

        if (medication.getFrequency() == MedicationFrequency.WEEKLY) {
            long daysBetween = ChronoUnit.DAYS.between(medication.getStartDate(), date);
            return daysBetween % 7 == 0;
        }

        return medication.getFrequency() == MedicationFrequency.AS_NEEDED
                && medication.getStartDate().isEqual(date);
    }

    public Optional<OffsetDateTime> calculateNextReminderAt(Medication medication) {
        if (!medication.isActive() || !medication.isReminderEnabled()) {
            return Optional.empty();
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneId.of(medication.getTimezone()));
        OffsetDateTime medicationAt = firstMedicationOccurrence(medication);

        while (medicationAt != null) {
            OffsetDateTime reminderAt = applyReminderOffset(medicationAt, medication.getReminderOffset());

            if (reminderAt.isAfter(now)) {
                return Optional.of(reminderAt);
            }

            medicationAt = nextMedicationOccurrence(medication, medicationAt);
        }

        return Optional.empty();
    }

    public OffsetDateTime applyReminderOffset(OffsetDateTime medicationAt, MedicationReminderOffset offset) {
        return switch (offset) {
            case ON_TIME -> medicationAt;
            case FIFTEEN_MINUTES_BEFORE -> medicationAt.minusMinutes(15);
            case THIRTY_MINUTES_BEFORE -> medicationAt.minusMinutes(30);
        };
    }

    public OffsetDateTime toOffsetDateTime(LocalDate date, LocalTime time, String timezone) {
        return date.atTime(time)
                .atZone(ZoneId.of(normalizeTimezone(timezone)))
                .toOffsetDateTime();
    }

    private OffsetDateTime firstMedicationOccurrence(Medication medication) {
        LocalDate today = today(medication.getTimezone());
        LocalDate candidateDate = switch (medication.getFrequency()) {
            case DAILY -> medication.getStartDate().isAfter(today) ? medication.getStartDate() : today;
            case WEEKLY -> nextWeeklyDateOnOrAfter(medication.getStartDate(), today);
            case AS_NEEDED -> medication.getStartDate();
        };

        return toOffsetDateTime(candidateDate, medication.getMedicationTime(), medication.getTimezone());
    }

    private LocalDate nextWeeklyDateOnOrAfter(LocalDate startDate, LocalDate targetDate) {
        if (!startDate.isBefore(targetDate)) {
            return startDate;
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, targetDate);
        long weeksToAdd = daysBetween / 7;
        LocalDate candidateDate = startDate.plusWeeks(weeksToAdd);

        if (candidateDate.isBefore(targetDate)) {
            return candidateDate.plusWeeks(1);
        }

        return candidateDate;
    }

    private OffsetDateTime nextMedicationOccurrence(Medication medication, OffsetDateTime currentOccurrence) {
        return switch (medication.getFrequency()) {
            case DAILY -> currentOccurrence.plusDays(1);
            case WEEKLY -> currentOccurrence.plusWeeks(1);
            case AS_NEEDED -> null;
        };
    }
}
