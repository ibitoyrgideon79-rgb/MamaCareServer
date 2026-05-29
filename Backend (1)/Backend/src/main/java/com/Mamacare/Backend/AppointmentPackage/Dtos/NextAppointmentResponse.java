package com.Mamacare.Backend.AppointmentPackage.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextAppointmentResponse {

    @JsonProperty("has_upcoming_appointment")
    private boolean hasUpcomingAppointment;

    private AppointmentResponse appointment;
}