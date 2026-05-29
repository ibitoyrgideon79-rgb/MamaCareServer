package com.Mamacare.Backend.AppointmentPackage.Repo;

import com.Mamacare.Backend.AppointmentPackage.Entity.AppointmentChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentChecklistItemRepo extends JpaRepository<AppointmentChecklistItem, Long> {

    List<AppointmentChecklistItem> findByAppointmentIdOrderBySortOrderAsc(Long appointmentId);

    Optional<AppointmentChecklistItem> findByIdAndAppointmentId(Long id, Long appointmentId);
}
