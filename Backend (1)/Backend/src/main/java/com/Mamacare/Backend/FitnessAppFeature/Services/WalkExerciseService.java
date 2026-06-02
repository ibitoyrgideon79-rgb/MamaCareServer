package com.Mamacare.Backend.FitnessAppFeature.Services;

import com.Mamacare.Backend.AuthenticationPackage.user.User;
import com.Mamacare.Backend.FitnessAppFeature.Dtos.StartWalkSessionRequest;
import com.Mamacare.Backend.FitnessAppFeature.Dtos.SyncWalkSessionMetricsRequest;
import com.Mamacare.Backend.FitnessAppFeature.Dtos.WalkHomeResponse;
import com.Mamacare.Backend.FitnessAppFeature.Dtos.WalkSessionResponse;
import com.Mamacare.Backend.FitnessAppFeature.Entity.WalkGoalSetting;
import com.Mamacare.Backend.FitnessAppFeature.Entity.WalkSession;
import com.Mamacare.Backend.FitnessAppFeature.Enums.WalkSessionStatus;
import com.Mamacare.Backend.FitnessAppFeature.Enums.WalkSourcePlatform;
import com.Mamacare.Backend.FitnessAppFeature.Repo.WalkGoalSettingRepo;
import com.Mamacare.Backend.FitnessAppFeature.Repo.WalkSessionRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WalkExerciseService {

    private static final String DEFAULT_TIMEZONE = "Africa/Lagos";
    private static final int DEFAULT_GOAL_MINUTES = 15;

    private final WalkSessionRepo walkSessionRepository;
    private final WalkGoalSettingRepo walkGoalSettingRepository;

    @Transactional(readOnly = true)
    public WalkHomeResponse getHome(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        WalkGoalSetting goalSetting = getGoalSettingOrDefault(currentUser);
        String timezone = normalizeTimezone(goalSetting.getTimezone());
        LocalDate today = LocalDate.now(ZoneId.of(timezone));

        List<WalkSession> todaySessions = walkSessionRepository
                .findByUserIdAndSessionDateOrderByStartedAtAsc(currentUser.getId(), today);
        WalkSession currentSession = walkSessionRepository
                .findFirstByUserIdAndStatusOrderByStartedAtDesc(currentUser.getId(), WalkSessionStatus.IN_PROGRESS)
                .orElse(null);

        int durationSeconds = todaySessions.stream().mapToInt(WalkSession::getDurationSeconds).sum();
        int completedMinutes = durationSeconds / 60;
        int goalMinutes = goalSetting.getDailyGoalMinutes();

        return WalkHomeResponse.builder()
                .currentSession(currentSession == null ? null : toResponse(currentSession))
                .todayGoal(WalkHomeResponse.TodayGoal.builder()
                        .completedMinutes(completedMinutes)
                        .goalMinutes(goalMinutes)
                        .progressPercent(progressPercent(completedMinutes, goalMinutes))
                        .label(completedMinutes + " / " + goalMinutes + " min")
                        .build())
                .todayMetrics(WalkHomeResponse.TodayMetrics.builder()
                        .steps(todaySessions.stream().mapToInt(WalkSession::getSteps).sum())
                        .distanceMeters(todaySessions.stream().mapToInt(WalkSession::getDistanceMeters).sum())
                        .displayDistance(displayDistance(todaySessions.stream().mapToInt(WalkSession::getDistanceMeters).sum()))
                        .caloriesKcal(todaySessions.stream().mapToInt(WalkSession::getCaloriesKcal).sum())
                        .build())
                .motivation("Let's move gently. You are doing great!")
                .audioGuides(defaultAudioGuides())
                .benefits(defaultBenefits())
                .bestTimes(defaultBestTimes())
                .build();
    }

    @Transactional
    public WalkSessionResponse startSession(StartWalkSessionRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        if (request != null && request.getSourceSessionId() != null && !request.getSourceSessionId().isBlank()) {
            return walkSessionRepository
                    .findByUserIdAndSourceSessionId(currentUser.getId(), request.getSourceSessionId().trim())
                    .map(this::toResponse)
                    .orElseGet(() -> createSession(currentUser, request));
        }

        return createSession(currentUser, request);
    }

    @Transactional
    public WalkSessionResponse syncMetrics(
            Long sessionId,
            SyncWalkSessionMetricsRequest request,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        WalkSession session = findSessionOwnedByUser(sessionId, currentUser.getId());
        applyMetrics(session, request);
        return toResponse(walkSessionRepository.save(session));
    }

    @Transactional
    public WalkSessionResponse completeSession(
            Long sessionId,
            SyncWalkSessionMetricsRequest request,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        WalkSession session = findSessionOwnedByUser(sessionId, currentUser.getId());
        applyMetrics(session, request);
        session.setStatus(WalkSessionStatus.COMPLETED);
        session.setEndedAt(OffsetDateTime.now(ZoneId.of(session.getTimezone())));
        return toResponse(walkSessionRepository.save(session));
    }

    private WalkSessionResponse createSession(User currentUser, StartWalkSessionRequest request) {
        WalkGoalSetting goalSetting = getGoalSettingOrDefault(currentUser);
        String timezone = normalizeTimezone(request == null ? null : request.getTimezone());
        int goalMinutes = request != null && request.getGoalMinutes() != null
                ? request.getGoalMinutes()
                : goalSetting.getDailyGoalMinutes();
        WalkSourcePlatform sourcePlatform = request != null && request.getSourcePlatform() != null
                ? request.getSourcePlatform()
                : WalkSourcePlatform.MANUAL;
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of(timezone));

        WalkSession session = WalkSession.builder()
                .user(currentUser)
                .sessionDate(now.toLocalDate())
                .startedAt(now)
                .status(WalkSessionStatus.IN_PROGRESS)
                .sourcePlatform(sourcePlatform)
                .sourceSessionId(normalizeText(request == null ? null : request.getSourceSessionId()))
                .timezone(timezone)
                .goalMinutes(goalMinutes)
                .build();

        return toResponse(walkSessionRepository.save(session));
    }

    private void applyMetrics(WalkSession session, SyncWalkSessionMetricsRequest request) {
        if (request == null) {
            return;
        }

        if (request.getDurationSeconds() != null) {
            session.setDurationSeconds(Math.max(session.getDurationSeconds(), request.getDurationSeconds()));
        }
        if (request.getSteps() != null) {
            session.setSteps(Math.max(session.getSteps(), request.getSteps()));
        }
        if (request.getDistanceMeters() != null) {
            session.setDistanceMeters(Math.max(session.getDistanceMeters(), request.getDistanceMeters()));
        }
        if (request.getCaloriesKcal() != null) {
            session.setCaloriesKcal(Math.max(session.getCaloriesKcal(), request.getCaloriesKcal()));
        }
    }

    private WalkSession findSessionOwnedByUser(Long sessionId, Integer userId) {
        return walkSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Walk session not found."));
    }

    private WalkGoalSetting getGoalSettingOrDefault(User user) {
        return walkGoalSettingRepository.findByUserId(user.getId())
                .orElseGet(() -> WalkGoalSetting.builder()
                        .user(user)
                        .dailyGoalMinutes(DEFAULT_GOAL_MINUTES)
                        .timezone(DEFAULT_TIMEZONE)
                        .build());
    }

    private WalkSessionResponse toResponse(WalkSession session) {
        int completedMinutes = session.getDurationSeconds() / 60;

        return WalkSessionResponse.builder()
                .id(session.getId())
                .status(session.getStatus())
                .sessionDate(session.getSessionDate())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .goalMinutes(session.getGoalMinutes())
                .durationSeconds(session.getDurationSeconds())
                .displayDuration(completedMinutes + " min")
                .progressPercent(progressPercent(completedMinutes, session.getGoalMinutes()))
                .steps(session.getSteps())
                .distanceMeters(session.getDistanceMeters())
                .displayDistance(displayDistance(session.getDistanceMeters()))
                .caloriesKcal(session.getCaloriesKcal())
                .sourcePlatform(session.getSourcePlatform())
                .build();
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
        return currentUser;
    }

    private String normalizeTimezone(String timezone) {
        String normalizedTimezone = timezone == null || timezone.isBlank() ? DEFAULT_TIMEZONE : timezone.trim();
        ZoneId.of(normalizedTimezone);
        return normalizedTimezone;
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int progressPercent(int completedMinutes, int goalMinutes) {
        if (goalMinutes <= 0) {
            return 0;
        }
        return Math.min(100, (completedMinutes * 100) / goalMinutes);
    }

    private String displayDistance(int distanceMeters) {
        return String.format(Locale.ENGLISH, "%.2f km", distanceMeters / 1000.0);
    }

    private List<WalkHomeResponse.AudioGuide> defaultAudioGuides() {
        return List.of(
                WalkHomeResponse.AudioGuide.builder()
                        .language("ENGLISH")
                        .title("Listen in English")
                        .audioUrl("/audio/walk-session-english.mp3")
                        .durationSeconds(75)
                        .build(),
                WalkHomeResponse.AudioGuide.builder()
                        .language("YORUBA")
                        .title("Listen in Yoruba")
                        .audioUrl("/audio/walk-session-yoruba.mp3")
                        .durationSeconds(75)
                        .build(),
                WalkHomeResponse.AudioGuide.builder()
                        .language("PIDGIN")
                        .title("Listen in Pidgin")
                        .audioUrl("/audio/walk-session-pidgin.mp3")
                        .durationSeconds(75)
                        .build()
        );
    }

    private List<WalkHomeResponse.InfoItem> defaultBenefits() {
        return List.of(
                WalkHomeResponse.InfoItem.builder().title("Boost energy").body("Gentle movement can help reduce tiredness.").build(),
                WalkHomeResponse.InfoItem.builder().title("Improves sleep").body("Walking can support better rest.").build(),
                WalkHomeResponse.InfoItem.builder().title("Reduces swelling").body("Movement can help blood flow in the legs.").build(),
                WalkHomeResponse.InfoItem.builder().title("Supports baby's growth").body("Healthy movement supports overall pregnancy wellness.").build()
        );
    }

    private List<WalkHomeResponse.BestTime> defaultBestTimes() {
        return List.of(
                WalkHomeResponse.BestTime.builder().label("Morning").window("7AM - 10AM").build(),
                WalkHomeResponse.BestTime.builder().label("Evening").window("4PM - 7PM").build()
        );
    }
}

