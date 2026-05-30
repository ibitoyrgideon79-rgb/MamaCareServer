package com.Mamacare.Backend.MedicationRemiderPackage.Service;

import com.Mamacare.Backend.MedicationRemiderPackage.Entity.Medication;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationFrequency;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicationScheduleCalculatorTest {

    private final MedicationScheduleCalculator calculator = new MedicationScheduleCalculator();

    @Test
    void shouldOccurOnReturnsTrueForDailyAfterStartDate() {
        Medication medication = medication(MedicationFrequency.DAILY, LocalDate.of(2026, 5, 1));

        assertThat(calculator.shouldOccurOn(medication, LocalDate.of(2026, 5, 30))).isTrue();
        assertThat(calculator.shouldOccurOn(medication, LocalDate.of(2026, 4, 30))).isFalse();
    }

    @Test
    void shouldOccurOnReturnsTrueForWeeklyOnlyEverySevenDays() {
        Medication medication = medication(MedicationFrequency.WEEKLY, LocalDate.of(2026, 5, 2));

        assertThat(calculator.shouldOccurOn(medication, LocalDate.of(2026, 5, 30))).isTrue();
        assertThat(calculator.shouldOccurOn(medication, LocalDate.of(2026, 5, 31))).isFalse();
    }

    @Test
    void shouldOccurOnReturnsTrueForAsNeededOnlyOnStartDate() {
        Medication medication = medication(MedicationFrequency.AS_NEEDED, LocalDate.of(2026, 5, 30));

        assertThat(calculator.shouldOccurOn(medication, LocalDate.of(2026, 5, 30))).isTrue();
        assertThat(calculator.shouldOccurOn(medication, LocalDate.of(2026, 5, 31))).isFalse();
    }

    @Test
    void applyReminderOffsetSubtractsCorrectMinutes() {
        OffsetDateTime medicationAt = OffsetDateTime.parse("2026-05-30T17:00:00+01:00");

        assertThat(calculator.applyReminderOffset(medicationAt, MedicationReminderOffset.ON_TIME))
                .isEqualTo(medicationAt);
        assertThat(calculator.applyReminderOffset(medicationAt, MedicationReminderOffset.FIFTEEN_MINUTES_BEFORE))
                .isEqualTo(OffsetDateTime.parse("2026-05-30T16:45:00+01:00"));
        assertThat(calculator.applyReminderOffset(medicationAt, MedicationReminderOffset.THIRTY_MINUTES_BEFORE))
                .isEqualTo(OffsetDateTime.parse("2026-05-30T16:30:00+01:00"));
    }

    @Test
    void normalizeTimezoneDefaultsBlankAndRejectsInvalidTimezone() {
        assertThat(calculator.normalizeTimezone("")).isEqualTo("Africa/Lagos");
        assertThat(calculator.normalizeTimezone(null)).isEqualTo("Africa/Lagos");

        assertThatThrownBy(() -> calculator.normalizeTimezone("Not/A_Timezone"))
                .isInstanceOf(DateTimeException.class);
    }

    @Test
    void calculateNextReminderAtReturnsEmptyWhenReminderIsDisabled() {
        Medication medication = medication(MedicationFrequency.DAILY, LocalDate.now(ZoneId.of("Africa/Lagos")));
        medication.setReminderEnabled(false);

        assertThat(calculator.calculateNextReminderAt(medication)).isEmpty();
    }

    @Test
    void calculateNextReminderAtReturnsFutureReminderForEnabledMedication() {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Africa/Lagos")).plusDays(1);
        Medication medication = medication(MedicationFrequency.DAILY, tomorrow);
        medication.setMedicationTime(LocalTime.of(17, 0));
        medication.setReminderOffset(MedicationReminderOffset.FIFTEEN_MINUTES_BEFORE);

        Optional<OffsetDateTime> reminderAt = calculator.calculateNextReminderAt(medication);

        assertThat(reminderAt).isPresent();
        assertThat(reminderAt.get().toLocalDate()).isEqualTo(tomorrow);
        assertThat(reminderAt.get().toLocalTime()).isEqualTo(LocalTime.of(16, 45));
    }

    private Medication medication(MedicationFrequency frequency, LocalDate startDate) {
        return Medication.builder()
                .id(1L)
                .medicineName("PregnaCare")
                .dose("2 tablets")
                .frequency(frequency)
                .medicationTime(LocalTime.of(17, 0))
                .startDate(startDate)
                .timezone("Africa/Lagos")
                .reminderEnabled(true)
                .reminderOffset(MedicationReminderOffset.ON_TIME)
                .active(true)
                .build();
    }
}
