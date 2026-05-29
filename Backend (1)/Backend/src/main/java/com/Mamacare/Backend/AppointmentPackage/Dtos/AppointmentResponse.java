package com.Mamacare.Backend.AppointmentPackage.Dtos;

import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentStatus;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentType;
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
public class AppointmentResponse {

    private Long id;

    @JsonProperty("appointment_type")
    private AppointmentType appointmentType;

    @JsonProperty("appointment_type_label")
    private String appointmentTypeLabel;

    private AppointmentStatus status;

    @JsonProperty("appointment_date")
    private LocalDate appointmentDate;

    @JsonProperty("appointment_time")
    private LocalTime appointmentTime;

    @JsonProperty("display_time")
    private String displayTime;

    @JsonProperty("location_name")
    private String locationName;

    private String notes;

    @JsonProperty("days_to_go")
    private long daysToGo;

    @JsonProperty("reminder_enabled")
    private boolean reminderEnabled;

    private List<AppointmentReminderResponse> reminders;

    private List<AppointmentChecklistItemResponse> checklist;
}
