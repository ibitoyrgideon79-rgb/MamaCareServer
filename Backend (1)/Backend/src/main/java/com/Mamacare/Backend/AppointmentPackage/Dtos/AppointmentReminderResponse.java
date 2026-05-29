package com.Mamacare.Backend.AppointmentPackage.Dtos;

import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentReminderOffset;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentReminderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentReminderResponse {

    private AppointmentReminderOffset offset;

    @JsonProperty("remind_at")
    private OffsetDateTime remindAt;

    private AppointmentReminderStatus status;
}