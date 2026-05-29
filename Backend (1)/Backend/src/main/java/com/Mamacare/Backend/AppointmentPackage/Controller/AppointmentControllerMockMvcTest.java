package com.Mamacare.Backend.AppointmentPackage.Controller;

import com.Mamacare.Backend.AppointmentPackage.Dtos.AppointmentChecklistItemResponse;
import com.Mamacare.Backend.AppointmentPackage.Dtos.AppointmentReminderResponse;
import com.Mamacare.Backend.AppointmentPackage.Dtos.AppointmentResponse;
import com.Mamacare.Backend.AppointmentPackage.Dtos.NextAppointmentResponse;
import com.Mamacare.Backend.AppointmentPackage.Dtos.UpdateChecklistItemRequest;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentReminderOffset;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentReminderStatus;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentStatus;
import com.Mamacare.Backend.AppointmentPackage.Enums.AppointmentType;
import com.Mamacare.Backend.AppointmentPackage.Service.AppointmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerMockMvcTest {

    private MockMvc mockMvc;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        AppointmentController controller = new AppointmentController(appointmentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createAppointmentReturnsRemindersAndChecklist() throws Exception {
        AppointmentResponse response = AppointmentResponse.builder()
                .id(10L)
                .appointmentType(AppointmentType.ANTENATAL)
                .appointmentTypeLabel("Antenatal Appointment")
                .status(AppointmentStatus.SCHEDULED)
                .appointmentDate(LocalDate.of(2026, 6, 10))
                .appointmentTime(LocalTime.of(10, 30))
                .displayTime("10.30 AM")
                .locationName("MamaCare Clinic, Lagos")
                .notes("Bring lab results and questions.")
                .daysToGo(12)
                .reminderEnabled(true)
                .reminders(List.of(
                        AppointmentReminderResponse.builder()
                                .offset(AppointmentReminderOffset.ONE_HOUR_BEFORE)
                                .remindAt(OffsetDateTime.parse("2026-06-10T09:30:00+01:00"))
                                .status(AppointmentReminderStatus.PENDING)
                                .build()
                ))
                .checklist(List.of(
                        AppointmentChecklistItemResponse.builder()
                                .id(100L)
                                .text("Prepare questions to ask your doctor.")
                                .completed(false)
                                .build()
                ))
                .build();

        when(appointmentService.createAppointment(any(), any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/appointments")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appointment_type": "ANTENATAL",
                                  "appointment_date": "2026-06-10",
                                  "appointment_time": "10:30:00",
                                  "timezone": "Africa/Lagos",
                                  "location_name": "MamaCare Clinic, Lagos",
                                  "notes": "Bring lab results and questions.",
                                  "reminder_enabled": true,
                                  "reminder_offsets": [
                                    "ONE_HOUR_BEFORE"
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.appointment_type").value("ANTENATAL"))
                .andExpect(jsonPath("$.reminder_enabled").value(true))
                .andExpect(jsonPath("$.reminders[0].offset").value("ONE_HOUR_BEFORE"))
                .andExpect(jsonPath("$.reminders[0].status").value("PENDING"))
                .andExpect(jsonPath("$.checklist[0].id").value(100))
                .andExpect(jsonPath("$.checklist[0].completed").value(false));
    }

    @Test
    void getNextAppointmentReturnsAppointmentWithRemindersAndChecklist() throws Exception {
        AppointmentResponse appointment = AppointmentResponse.builder()
                .id(10L)
                .appointmentType(AppointmentType.ANTENATAL)
                .appointmentTypeLabel("Antenatal Appointment")
                .status(AppointmentStatus.SCHEDULED)
                .appointmentDate(LocalDate.of(2026, 6, 10))
                .appointmentTime(LocalTime.of(10, 30))
                .displayTime("10.30 AM")
                .locationName("MamaCare Clinic, Lagos")
                .notes("Bring lab results and questions.")
                .daysToGo(12)
                .reminderEnabled(true)
                .reminders(List.of(
                        AppointmentReminderResponse.builder()
                                .offset(AppointmentReminderOffset.ONE_HOUR_BEFORE)
                                .remindAt(OffsetDateTime.parse("2026-06-10T09:30:00+01:00"))
                                .status(AppointmentReminderStatus.PENDING)
                                .build()
                ))
                .checklist(List.of(
                        AppointmentChecklistItemResponse.builder()
                                .id(100L)
                                .text("Prepare questions to ask your doctor.")
                                .completed(false)
                                .build()
                ))
                .build();

        NextAppointmentResponse response = NextAppointmentResponse.builder()
                .hasUpcomingAppointment(true)
                .appointment(appointment)
                .build();

        when(appointmentService.getNextAppointment(any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/appointments/upcoming/next")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.has_upcoming_appointment").value(true))
                .andExpect(jsonPath("$.appointment.id").value(10))
                .andExpect(jsonPath("$.appointment.reminders[0].offset").value("ONE_HOUR_BEFORE"))
                .andExpect(jsonPath("$.appointment.checklist[0].completed").value(false));
    }

    @Test
    void updateChecklistItemReturnsUpdatedItem() throws Exception {
        AppointmentChecklistItemResponse response = AppointmentChecklistItemResponse.builder()
                .id(100L)
                .text("Prepare questions to ask your doctor.")
                .completed(true)
                .build();

        when(appointmentService.updateChecklistItem(eq(10L), eq(100L), any(), any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/appointments/{appointmentId}/checklist/{itemId}", 10L, 100L)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "completed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.text").value("Prepare questions to ask your doctor."))
                .andExpect(jsonPath("$.completed").value(true));
    }
}
