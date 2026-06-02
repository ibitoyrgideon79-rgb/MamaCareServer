package com.Mamacare.Backend.FitnessAppFeature.Repo;

import com.Mamacare.Backend.FitnessAppFeature.Entity.WalkGoalSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalkGoalSettingRepo extends JpaRepository<WalkGoalSetting, Long> {

    Optional<WalkGoalSetting> findByUserId(Integer userId);
}
