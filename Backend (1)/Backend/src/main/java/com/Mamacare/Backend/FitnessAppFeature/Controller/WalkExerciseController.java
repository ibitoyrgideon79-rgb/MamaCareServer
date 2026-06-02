package com.Mamacare.Backend.FitnessAppFeature.Controller;

import com.Mamacare.Backend.FitnessAppFeature.Dtos.StartWalkSessionRequest;
import com.Mamacare.Backend.FitnessAppFeature.Dtos.SyncWalkSessionMetricsRequest;
import com.Mamacare.Backend.FitnessAppFeature.Dtos.WalkHomeResponse;
import com.Mamacare.Backend.FitnessAppFeature.Dtos.WalkSessionResponse;
import com.Mamacare.Backend.FitnessAppFeature.Services.WalkExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/walk-exercise")
@RequiredArgsConstructor
public class WalkExerciseController {

    private final WalkExerciseService walkExerciseService;

    @GetMapping("/home")
    public ResponseEntity<WalkHomeResponse> getHome(Authentication authentication) {
        return ResponseEntity.ok(walkExerciseService.getHome(authentication));
    }

    @PostMapping("/sessions")
    public ResponseEntity<WalkSessionResponse> startSession(
            @Valid @RequestBody(required = false) StartWalkSessionRequest request,
            Authentication authentication
    ) {
        WalkSessionResponse response = walkExerciseService.startSession(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/sessions/{sessionId}/metrics")
    public ResponseEntity<WalkSessionResponse> syncMetrics(
            @PathVariable Long sessionId,
            @Valid @RequestBody SyncWalkSessionMetricsRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(walkExerciseService.syncMetrics(sessionId, request, authentication));
    }

    @PatchMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<WalkSessionResponse> completeSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody(required = false) SyncWalkSessionMetricsRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(walkExerciseService.completeSession(sessionId, request, authentication));
    }
}
