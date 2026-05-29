package com.Mamacare.Backend.AppointmentPackage.Repo;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.Mamacare.Backend.AppointmentPackage.Entity.Appointment;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentStatus;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findFirstByUserIdAndStatusAndScheduledStartAtGreaterThanEqualOrderByScheduledStartAtAsc(
            Integer userId,
            AppointmentStatus status,
            OffsetDateTime now
    );

    Optional<Appointment> findByIdAndUserId(Long id, Integer userId);
}