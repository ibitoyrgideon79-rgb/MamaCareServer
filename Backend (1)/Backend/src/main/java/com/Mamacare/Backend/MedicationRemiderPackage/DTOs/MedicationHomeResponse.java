package com.Mamacare.Backend.MedicationRemiderPackage.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationHomeResponse {

    @JsonProperty("today_medications")
    private List<MedicationResponse> todayMedications;

    private List<MedicationTipResponse> tips;
}
