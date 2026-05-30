package com.Mamacare.Backend.MedicationRemiderPackage.Entity;

import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderStatus;
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
        name = "medication_reminders",
        indexes = {
                @Index(name = "idx_medication_reminders_due_status", columnList = "remind_at, status"),
                @Index(name = "idx_medication_reminders_medication", columnList = "medication_id")
        }
)
public class MedicationReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_offset", nullable = false, length = 40)
    private MedicationReminderOffset reminderOffset;

    @Column(name = "remind_at", nullable = false)
    private OffsetDateTime remindAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private MedicationReminderStatus status = MedicationReminderStatus.PENDING;
}
