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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalkExerciseServiceTest {

    private WalkExerciseService walkExerciseService;

    @Mock
    private WalkSessionRepo walkSessionRepository;

    @Mock
    private WalkGoalSettingRepo walkGoalSettingRepository;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        walkExerciseService = new WalkExerciseService(walkSessionRepository, walkGoalSettingRepository);
    }

    @Test
    void startSessionCreatesInProgressSessionWithTrimmedSourceId() {
        User user = user();
        StartWalkSessionRequest request = StartWalkSessionRequest.builder()
                .goalMinutes(25)
                .timezone(" Africa/Lagos ")
                .sourcePlatform(WalkSourcePlatform.ANDROID)
                .sourceSessionId(" phone-session-1 ")
                .build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(walkSessionRepository.findByUserIdAndSourceSessionId(user.getId(), "phone-session-1"))
                .thenReturn(Optional.empty());
        when(walkGoalSettingRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(walkSessionRepository.save(any(WalkSession.class))).thenAnswer(invocation -> {
            WalkSession session = invocation.getArgument(0);
            session.setId(1L);
            return session;
        });

        WalkSessionResponse response = walkExerciseService.startSession(request, authentication);

        ArgumentCaptor<WalkSession> sessionCaptor = ArgumentCaptor.forClass(WalkSession.class);
        verify(walkSessionRepository).save(sessionCaptor.capture());
        WalkSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getUser()).isEqualTo(user);
        assertThat(savedSession.getStatus()).isEqualTo(WalkSessionStatus.IN_PROGRESS);
        assertThat(savedSession.getGoalMinutes()).isEqualTo(25);
        assertThat(savedSession.getTimezone()).isEqualTo("Africa/Lagos");
        assertThat(savedSession.getSourcePlatform()).isEqualTo(WalkSourcePlatform.ANDROID);
        assertThat(savedSession.getSourceSessionId()).isEqualTo("phone-session-1");
        assertThat(savedSession.getSessionDate()).isEqualTo(savedSession.getStartedAt().toLocalDate());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(WalkSessionStatus.IN_PROGRESS);
        assertThat(response.getGoalMinutes()).isEqualTo(25);
        assertThat(response.getProgressPercent()).isZero();
        assertThat(response.getDisplayDistance()).isEqualTo("0.00 km");
    }

    @Test
    void startSessionReturnsExistingSessionForSameSourceSessionId() {
        User user = user();
        StartWalkSessionRequest request = StartWalkSessionRequest.builder()
                .sourceSessionId(" phone-session-1 ")
                .build();
        WalkSession existingSession = session(9L, user);
        existingSession.setSourceSessionId("phone-session-1");
        existingSession.setDurationSeconds(600);

        when(authentication.getPrincipal()).thenReturn(user);
        when(walkSessionRepository.findByUserIdAndSourceSessionId(user.getId(), "phone-session-1"))
                .thenReturn(Optional.of(existingSession));

        WalkSessionResponse response = walkExerciseService.startSession(request, authentication);

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getDisplayDuration()).isEqualTo("10 min");
        verify(walkSessionRepository, never()).save(any(WalkSession.class));
    }

    @Test
    void syncMetricsKeepsHighestMetricValuesOnly() {
        User user = user();
        WalkSession existingSession = session(1L, user);
        existingSession.setDurationSeconds(600);
        existingSession.setSteps(900);
        existingSession.setDistanceMeters(700);
        existingSession.setCaloriesKcal(80);
        SyncWalkSessionMetricsRequest request = SyncWalkSessionMetricsRequest.builder()
                .durationSeconds(540)
                .steps(1200)
                .distanceMeters(650)
                .caloriesKcal(100)
                .build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(walkSessionRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(existingSession));
        when(walkSessionRepository.save(existingSession)).thenReturn(existingSession);

        WalkSessionResponse response = walkExerciseService.syncMetrics(1L, request, authentication);

        assertThat(existingSession.getDurationSeconds()).isEqualTo(600);
        assertThat(existingSession.getSteps()).isEqualTo(1200);
        assertThat(existingSession.getDistanceMeters()).isEqualTo(700);
        assertThat(existingSession.getCaloriesKcal()).isEqualTo(100);
        assertThat(response.getDisplayDuration()).isEqualTo("10 min");
        assertThat(response.getDisplayDistance()).isEqualTo("0.70 km");
        verify(walkSessionRepository).save(existingSession);
    }

    @Test
    void completeSessionAppliesMetricsAndMarksCompleted() {
        User user = user();
        WalkSession existingSession = session(1L, user);
        SyncWalkSessionMetricsRequest request = SyncWalkSessionMetricsRequest.builder()
                .durationSeconds(960)
                .steps(1800)
                .distanceMeters(1250)
                .caloriesKcal(130)
                .build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(walkSessionRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(existingSession));
        when(walkSessionRepository.save(existingSession)).thenReturn(existingSession);

        WalkSessionResponse response = walkExerciseService.completeSession(1L, request, authentication);

        assertThat(existingSession.getStatus()).isEqualTo(WalkSessionStatus.COMPLETED);
        assertThat(existingSession.getEndedAt()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(WalkSessionStatus.COMPLETED);
        assertThat(response.getDurationSeconds()).isEqualTo(960);
        assertThat(response.getProgressPercent()).isEqualTo(100);
        assertThat(response.getDisplayDistance()).isEqualTo("1.25 km");
    }

    @Test
    void getHomeAggregatesTodaySessionsAndCurrentSession() {
        User user = user();
        WalkGoalSetting goalSetting = WalkGoalSetting.builder()
                .user(user)
                .dailyGoalMinutes(30)
                .timezone("Africa/Lagos")
                .build();
        WalkSession morningSession = session(1L, user);
        morningSession.setDurationSeconds(600);
        morningSession.setSteps(1000);
        morningSession.setDistanceMeters(800);
        morningSession.setCaloriesKcal(70);
        WalkSession eveningSession = session(2L, user);
        eveningSession.setDurationSeconds(900);
        eveningSession.setSteps(1400);
        eveningSession.setDistanceMeters(1100);
        eveningSession.setCaloriesKcal(95);
        WalkSession currentSession = session(3L, user);

        when(authentication.getPrincipal()).thenReturn(user);
        when(walkGoalSettingRepository.findByUserId(user.getId())).thenReturn(Optional.of(goalSetting));
        when(walkSessionRepository.findByUserIdAndSessionDateOrderByStartedAtAsc(any(), any()))
                .thenReturn(List.of(morningSession, eveningSession));
        when(walkSessionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(
                user.getId(),
                WalkSessionStatus.IN_PROGRESS
        )).thenReturn(Optional.of(currentSession));

        WalkHomeResponse response = walkExerciseService.getHome(authentication);

        assertThat(response.getCurrentSession().getId()).isEqualTo(3L);
        assertThat(response.getTodayGoal().getCompletedMinutes()).isEqualTo(25);
        assertThat(response.getTodayGoal().getGoalMinutes()).isEqualTo(30);
        assertThat(response.getTodayGoal().getProgressPercent()).isEqualTo(83);
        assertThat(response.getTodayGoal().getLabel()).isEqualTo("25 / 30 min");
        assertThat(response.getTodayMetrics().getSteps()).isEqualTo(2400);
        assertThat(response.getTodayMetrics().getDistanceMeters()).isEqualTo(1900);
        assertThat(response.getTodayMetrics().getDisplayDistance()).isEqualTo("1.90 km");
        assertThat(response.getTodayMetrics().getCaloriesKcal()).isEqualTo(165);
        assertThat(response.getAudioGuides()).hasSize(3);
        assertThat(response.getBenefits()).hasSize(4);
        assertThat(response.getBestTimes()).hasSize(2);
    }

    @Test
    void getHomeUsesDefaultGoalWhenUserHasNoGoalSetting() {
        User user = user();

        when(authentication.getPrincipal()).thenReturn(user);
        when(walkGoalSettingRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(walkSessionRepository.findByUserIdAndSessionDateOrderByStartedAtAsc(any(), any()))
                .thenReturn(List.of());
        when(walkSessionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(
                user.getId(),
                WalkSessionStatus.IN_PROGRESS
        )).thenReturn(Optional.empty());

        WalkHomeResponse response = walkExerciseService.getHome(authentication);

        assertThat(response.getCurrentSession()).isNull();
        assertThat(response.getTodayGoal().getCompletedMinutes()).isZero();
        assertThat(response.getTodayGoal().getGoalMinutes()).isEqualTo(15);
        assertThat(response.getTodayGoal().getProgressPercent()).isZero();
        assertThat(response.getTodayMetrics().getDisplayDistance()).isEqualTo("0.00 km");
    }

    @Test
    void syncMetricsThrowsWhenSessionDoesNotBelongToUser() {
        User user = user();

        when(authentication.getPrincipal()).thenReturn(user);
        when(walkSessionRepository.findByIdAndUserId(404L, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walkExerciseService.syncMetrics(404L, null, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Walk session not found.");
    }

    @Test
    void startSessionRequiresAuthenticatedUser() {
        when(authentication.getPrincipal()).thenReturn("not-a-user");

        assertThatThrownBy(() -> walkExerciseService.startSession(null, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authenticated user is required.");
    }

    private User user() {
        return User.builder()
                .id(7)
                .email("mama@example.com")
                .build();
    }

    private WalkSession session(Long id, User user) {
        return WalkSession.builder()
                .id(id)
                .user(user)
                .sessionDate(LocalDate.of(2026, 6, 2))
                .startedAt(OffsetDateTime.parse("2026-06-02T08:00:00+01:00"))
                .status(WalkSessionStatus.IN_PROGRESS)
                .sourcePlatform(WalkSourcePlatform.MANUAL)
                .timezone("Africa/Lagos")
                .goalMinutes(15)
                .build();
    }
}
