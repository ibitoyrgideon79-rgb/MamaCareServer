package com.Mamacare.Backend.MedicationRemiderPackage.Entity;


import com.Mamacare.Backend.AuthenticationPackage.user.User;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationFrequency;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "medications",
        indexes = {
                @Index(name = "idx_medications_user_active", columnList = "user_id, active"),
                @Index(name = "idx_medications_user_time", columnList = "user_id, medication_time")
        }
)
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "medicine_name", nullable = false, length = 120)
    private String medicineName;

    @Column(nullable = false, length = 80)
    private String dose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MedicationFrequency frequency;

    @Column(name = "medication_time", nullable = false)
    private LocalTime medicationTime;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String timezone = "Africa/Lagos";

    @Column(name = "reminder_enabled", nullable = false)
    private boolean reminderEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_offset", nullable = false, length = 40)
    @Builder.Default
    private MedicationReminderOffset reminderOffset = MedicationReminderOffset.ON_TIME;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}