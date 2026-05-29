package com.Mamacare.Backend.AppointmentPackage.Dtos;

import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentReminderOffset;
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
public class CreateAppointmentRequest {

    @JsonProperty("appointment_type")
    private AppointmentType appointmentType;

    @JsonProperty("appointment_date")
    private LocalDate appointmentDate;

    @JsonProperty("appointment_time")
    private LocalTime appointmentTime;

    private String timezone;

    @JsonProperty("location_name")
    private String locationName;

    private String notes;

    @JsonProperty("reminder_enabled")
    private boolean reminderEnabled;

    @JsonProperty("reminder_offsets")
    private List<AppointmentReminderOffset> reminderOffsets;
}

