package com.Mamacare.Backend.FitnessAppFeature.Entity;

import com.Mamacare.Backend.AuthenticationPackage.user.User;
import com.Mamacare.Backend.FitnessAppFeature.Enums.WalkSessionStatus;
import com.Mamacare.Backend.FitnessAppFeature.Enums.WalkSourcePlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Entity
@Table(
        name = "walk_sessions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_walk_session_user_source", columnNames = {"user_id", "source_session_id"})
        },
        indexes = {
                @Index(name = "idx_walk_sessions_user_date", columnList = "user_id, session_date"),
                @Index(name = "idx_walk_sessions_user_status", columnList = "user_id, status")
        }
)
public class WalkSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private WalkSessionStatus status = WalkSessionStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_platform", nullable = false, length = 20)
    @Builder.Default
    private WalkSourcePlatform sourcePlatform = WalkSourcePlatform.MANUAL;

    @Column(name = "source_session_id", length = 120)
    private String sourceSessionId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String timezone = "Africa/Lagos";

    @Column(name = "goal_minutes", nullable = false)
    @Builder.Default
    private int goalMinutes = 15;

    @Column(name = "duration_seconds", nullable = false)
    @Builder.Default
    private int durationSeconds = 0;

    @Column(nullable = false)
    @Builder.Default
    private int steps = 0;

    @Column(name = "distance_meters", nullable = false)
    @Builder.Default
    private int distanceMeters = 0;

    @Column(name = "calories_kcal", nullable = false)
    @Builder.Default
    private int caloriesKcal = 0;
}