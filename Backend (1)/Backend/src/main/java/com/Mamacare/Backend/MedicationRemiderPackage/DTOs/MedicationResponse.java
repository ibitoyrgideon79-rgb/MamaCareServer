package com.Mamacare.Backend.MedicationRemiderPackage.DTOs;

import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationFrequency;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationResponse {

    private Long id;

    @JsonProperty("medicine_name")
    private String medicineName;

    private String dose;

    private MedicationFrequency frequency;

    @JsonProperty("medication_time")
    private LocalTime medicationTime;

    @JsonProperty("display_time")
    private String displayTime;

    @JsonProperty("start_date")
    private LocalDate startDate;

    private String timezone;

    private String notes;

    @JsonProperty("reminder_enabled")
    private boolean reminderEnabled;

    @JsonProperty("reminder_text")
    private String reminderText;

    @JsonProperty("taken_today")
    private boolean takenToday;

    private List<MedicationReminderResponse> reminders;
}
