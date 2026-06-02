package com.Mamacare.Backend.FitnessAppFeature.Dtos;

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
public class WalkHomeResponse {

    @JsonProperty("current_session")
    private WalkSessionResponse currentSession;

    @JsonProperty("today_goal")
    private TodayGoal todayGoal;

    @JsonProperty("today_metrics")
    private TodayMetrics todayMetrics;

    private String motivation;

    @JsonProperty("audio_guides")
    private List<AudioGuide> audioGuides;

    private List<InfoItem> benefits;

    @JsonProperty("best_times")
    private List<BestTime> bestTimes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayGoal {
        @JsonProperty("completed_minutes")
        private int completedMinutes;

        @JsonProperty("goal_minutes")
        private int goalMinutes;

        @JsonProperty("progress_percent")
        private int progressPercent;

        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayMetrics {
        private int steps;

        @JsonProperty("distance_meters")
        private int distanceMeters;

        @JsonProperty("display_distance")
        private String displayDistance;

        @JsonProperty("calories_kcal")
        private int caloriesKcal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioGuide {
        private String language;

        private String title;

        @JsonProperty("audio_url")
        private String audioUrl;

        @JsonProperty("duration_seconds")
        private int durationSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfoItem {
        private String title;
        private String body;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BestTime {
        private String label;
        private String window;
    }
}
