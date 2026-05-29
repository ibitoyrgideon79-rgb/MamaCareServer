package com.Mamacare.Backend.AppointmentPackage.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentChecklistItemResponse {

    private Long id;

    private String text;

    private boolean completed;
}
