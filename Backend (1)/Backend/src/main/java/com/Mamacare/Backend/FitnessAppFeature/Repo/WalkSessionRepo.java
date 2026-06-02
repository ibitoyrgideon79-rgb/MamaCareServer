package com.Mamacare.Backend.FitnessAppFeature.Repo;

import com.Mamacare.Backend.FitnessAppFeature.Entity.WalkSession;
import com.Mamacare.Backend.FitnessAppFeature.Enums.WalkSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WalkSessionRepo extends JpaRepository<WalkSession, Long> {

    List<WalkSession> findByUserIdAndSessionDateOrderByStartedAtAsc(Integer userId, LocalDate sessionDate);

    Optional<WalkSession> findFirstByUserIdAndStatusOrderByStartedAtDesc(Integer userId, WalkSessionStatus status);

    Optional<WalkSession> findByUserIdAndSourceSessionId(Integer userId, String sourceSessionId);

    Optional<WalkSession> findByIdAndUserId(Long id, Integer userId);
}
