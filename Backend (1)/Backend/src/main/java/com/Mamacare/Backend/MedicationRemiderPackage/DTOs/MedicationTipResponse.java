package com.Mamacare.Backend.MedicationRemiderPackage.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationTipResponse {

    private String title;

    private String body;
}