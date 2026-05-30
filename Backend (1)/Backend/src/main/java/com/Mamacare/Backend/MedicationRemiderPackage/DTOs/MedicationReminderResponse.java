package com.Mamacare.Backend.MedicationRemiderPackage.DTOs;

import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderStatus;
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
public class MedicationReminderResponse {

    private MedicationReminderOffset offset;

    @JsonProperty("remind_at")
    private OffsetDateTime remindAt;

    private MedicationReminderStatus status;
}

