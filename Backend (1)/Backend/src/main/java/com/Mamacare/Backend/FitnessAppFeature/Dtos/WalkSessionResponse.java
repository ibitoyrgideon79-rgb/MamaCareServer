package com.Mamacare.Backend.FitnessAppFeature.Dtos;

import com.Mamacare.Backend.FitnessAppFeature.Enums.WalkSessionStatus;
import com.Mamacare.Backend.FitnessAppFeature.Enums.WalkSourcePlatform;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkSessionResponse {

    private Long id;

    private WalkSessionStatus status;

    @JsonProperty("session_date")
    private LocalDate sessionDate;

    @JsonProperty("started_at")
    private OffsetDateTime startedAt;

    @JsonProperty("ended_at")
    private OffsetDateTime endedAt;

    @JsonProperty("goal_minutes")
    private int goalMinutes;

    @JsonProperty("duration_seconds")
    private int durationSeconds;

    @JsonProperty("display_duration")
    private String displayDuration;

    @JsonProperty("progress_percent")
    private int progressPercent;

    private int steps;

    @JsonProperty("distance_meters")
    private int distanceMeters;

    @JsonProperty("display_distance")
    private String displayDistance;

    @JsonProperty("calories_kcal")
    private int caloriesKcal;

    @JsonProperty("source_platform")
    private WalkSourcePlatform sourcePlatform;
}
