package com.Mamacare.Backend.FitnessAppFeature.Entity;

import com.Mamacare.Backend.AuthenticationPackage.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "walk_goal_settings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_walk_goal_user", columnNames = "user_id")
        }
)
public class WalkGoalSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "daily_goal_minutes", nullable = false)
    @Builder.Default
    private int dailyGoalMinutes = 15;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String timezone = "Africa/Lagos";
}