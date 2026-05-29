package com.Mamacare.Backend.AppointmentPackage.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Mamacare.Backend.AppointmentPackage.Entity.AppointmentReminder;
import java.util.List;

public interface AppointmentReminderRepo extends JpaRepository<AppointmentReminder, Long> {

    List<AppointmentReminder> findByAppointmentIdOrderByRemindAtAsc(Long appointmentId);
}