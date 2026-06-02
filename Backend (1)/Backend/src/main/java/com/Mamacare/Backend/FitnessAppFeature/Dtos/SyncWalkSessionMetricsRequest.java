package com.Mamacare.Backend.FitnessAppFeature.Dtos;

import jakarta.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncWalkSessionMetricsRequest {

    @Min(value = 0, message = "Duration seconds cannot be negative.")
    @Max(value = 86400, message = "Duration seconds must not exceed one day.")
    @JsonProperty("duration_seconds")
    private Integer durationSeconds;

    @Min(value = 0, message = "Steps cannot be negative.")
    @Max(value = 1000000, message = "Steps value is too large.")
    private Integer steps;

    @Min(value = 0, message = "Distance cannot be negative.")
    @JsonProperty("distance_meters")
    private Integer distanceMeters;

    @Min(value = 0, message = "Calories cannot be negative.")
    @Max(value = 5000, message = "Calories value is too large.")
    @JsonProperty("calories_kcal")
    private Integer caloriesKcal;
}
