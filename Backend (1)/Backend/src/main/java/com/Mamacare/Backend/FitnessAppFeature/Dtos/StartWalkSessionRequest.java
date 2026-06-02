package com.Mamacare.Backend.FitnessAppFeature.Dtos;

import jakarta.validation.constraints.Min;
import com.Mamacare.Backend.FitnessAppFeature.Enums.WalkSourcePlatform;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartWalkSessionRequest {
    
  @Min(value = 1, message = "Goal minutes must be at least 1.")
    @Max(value = 180, message = "Goal minutes must not exceed 180.")
    @JsonProperty("goal_minutes")
    private Integer goalMinutes;

    @Size(max = 50, message = "Timezone must not exceed 50 characters.")
    private String timezone;

    @JsonProperty("source_platform")
    private WalkSourcePlatform sourcePlatform;

    @Size(max = 120, message = "Source session id must not exceed 120 characters.")
    @JsonProperty("source_session_id")
    private String sourceSessionId;
}