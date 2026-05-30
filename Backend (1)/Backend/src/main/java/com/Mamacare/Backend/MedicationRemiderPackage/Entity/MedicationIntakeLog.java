package com.Mamacare.Backend.MedicationRemiderPackage.Entity;

import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationIntakeStatus;
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
        name = "medication_intake_logs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_medication_intake_day", columnNames = {"medication_id", "intake_date"})
        },
        indexes = {
                @Index(name = "idx_medication_intake_date", columnList = "intake_date")
        }
)
public class MedicationIntakeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "intake_date", nullable = false)
    private LocalDate intakeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MedicationIntakeStatus status;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;
}
