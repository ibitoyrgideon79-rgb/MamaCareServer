package com.Mamacare.Backend.MedicationRemiderPackage.DTOs;

import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationFrequency;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicationRequest {

    @NotBlank(message = "Medicine name is required.")
    @Size(max = 120, message = "Medicine name must not exceed 120 characters.")
    @JsonProperty("medicine_name")
    private String medicineName;

    @NotBlank(message = "Dose is required.")
    @Size(max = 80, message = "Dose must not exceed 80 characters.")
    private String dose;

    @NotNull(message = "Frequency is required.")
    private MedicationFrequency frequency;

    @NotNull(message = "Medication time is required.")
    @JsonProperty("medication_time")
    private LocalTime medicationTime;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @Size(max = 50, message = "Timezone must not exceed 50 characters.")
    private String timezone;

    @JsonProperty("reminder_enabled")
    private boolean reminderEnabled;

    @JsonProperty("reminder_offset")
    private MedicationReminderOffset reminderOffset;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters.")
    private String notes;
}
