package com.Mamacare.Backend.AppointmentPackage.Entity;

import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentStatus;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentType;
import com.Mamacare.Backend.AuthenticationPackage.user.User;
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

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(name = "idx_appointments_user_start", columnList = "user_id, scheduled_start_at"),
                @Index(name = "idx_appointments_user_status", columnList = "user_id, status")
        }
)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false, length = 40)
    private AppointmentType appointmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "scheduled_start_at", nullable = false)
    private OffsetDateTime scheduledStartAt;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String timezone = "Africa/Lagos";

    @Column(name = "location_name", nullable = false, length = 180)
    private String locationName;

    @Column(length = 1000)
    private String notes;

    @Column(name = "reminder_enabled", nullable = false)
    private boolean reminderEnabled;
}