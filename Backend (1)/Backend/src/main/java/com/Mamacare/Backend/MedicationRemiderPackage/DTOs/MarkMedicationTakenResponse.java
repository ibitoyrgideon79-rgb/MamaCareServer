package com.Mamacare.Backend.MedicationRemiderPackage.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkMedicationTakenResponse {

    @JsonProperty("medication_id")
    private Long medicationId;

    @JsonProperty("intake_date")
    private LocalDate intakeDate;

    @JsonProperty("taken_today")
    private boolean takenToday;
}
