package com.Mamacare.Backend.MedicationRemiderPackage.Controller;

import com.Mamacare.Backend.Commons.ApiExceptionHandler;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MarkMedicationTakenResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationHomeResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationReminderResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationTipResponse;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationFrequency;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderOffset;
import com.Mamacare.Backend.MedicationRemiderPackage.Enums.MedicationReminderStatus;
import com.Mamacare.Backend.MedicationRemiderPackage.Service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MedicationControllerMockMvcTest {

    private MockMvc mockMvc;

    @Mock
    private MedicationService medicationService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        MedicationController controller = new MedicationController(medicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createMedicationReturnsCreatedMedication() throws Exception {
        MedicationResponse response = medicationResponse(false);

        when(medicationService.createMedication(any(), any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/medications")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "medicine_name": "PregnaCare",
                                  "dose": "2 tablets",
                                  "frequency": "DAILY",
                                  "medication_time": "17:00:00",
                                  "start_date": "2026-05-30",
                                  "timezone": "Africa/Lagos",
                                  "reminder_enabled": true,
                                  "reminder_offset": "ON_TIME",
                                  "notes": "After dinner"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.medicine_name").value("PregnaCare"))
                .andExpect(jsonPath("$.dose").value("2 tablets"))
                .andExpect(jsonPath("$.frequency").value("DAILY"))
                .andExpect(jsonPath("$.medication_time").value("17:00:00"))
                .andExpect(jsonPath("$.display_time").value("5.00 PM"))
                .andExpect(jsonPath("$.timezone").value("Africa/Lagos"))
                .andExpect(jsonPath("$.reminder_enabled").value(true))
                .andExpect(jsonPath("$.reminder_text").value("We will remind you at 5.00 PM every day"))
                .andExpect(jsonPath("$.taken_today").value(false))
                .andExpect(jsonPath("$.reminders[0].offset").value("ON_TIME"))
                .andExpect(jsonPath("$.reminders[0].status").value("PENDING"));
    }

    @Test
    void createMedicationRejectsInvalidRequestBeforeServiceCall() throws Exception {
        mockMvc.perform(post("/api/v1/medications")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "medicine_name": "",
                                  "dose": "",
                                  "frequency": "DAILY",
                                  "medication_time": "17:00:00",
                                  "reminder_enabled": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());

        verifyNoInteractions(medicationService);
    }

    @Test
    void getTodayMedicationsReturnsHomeData() throws Exception {
        MedicationHomeResponse response = MedicationHomeResponse.builder()
                .todayMedications(List.of(medicationResponse(true)))
                .tips(List.of(MedicationTipResponse.builder()
                        .title("Take with water")
                        .body("Water helps many medicines move through the body safely.")
                        .build()))
                .build();

        when(medicationService.getTodayMedications(any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/medications/today")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.today_medications").isArray())
                .andExpect(jsonPath("$.today_medications[0].medicine_name").value("PregnaCare"))
                .andExpect(jsonPath("$.today_medications[0].taken_today").value(true))
                .andExpect(jsonPath("$.tips[0].title").value("Take with water"));
    }

    @Test
    void getAllMedicationsReturnsActiveMedicationList() throws Exception {
        when(medicationService.getAllMedications(any(Authentication.class)))
                .thenReturn(List.of(medicationResponse(false)));

        mockMvc.perform(get("/api/v1/medications")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].medicine_name").value("PregnaCare"));
    }

    @Test
    void markTakenReturnsTakenResponse() throws Exception {
        MarkMedicationTakenResponse response = MarkMedicationTakenResponse.builder()
                .medicationId(1L)
                .intakeDate(LocalDate.of(2026, 5, 30))
                .takenToday(true)
                .build();

        when(medicationService.markTaken(eq(1L), any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/medications/{medicationId}/taken", 1L)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medication_id").value(1))
                .andExpect(jsonPath("$.intake_date").value("2026-05-30"))
                .andExpect(jsonPath("$.taken_today").value(true));
    }

    @Test
    void deactivateMedicationReturnsNoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/medications/{medicationId}/inactive", 1L)
                        .principal(authentication))
                .andExpect(status().isNoContent());

        verify(medicationService).deactivateMedication(eq(1L), any(Authentication.class));
    }

    private MedicationResponse medicationResponse(boolean takenToday) {
        return MedicationResponse.builder()
                .id(1L)
                .medicineName("PregnaCare")
                .dose("2 tablets")
                .frequency(MedicationFrequency.DAILY)
                .medicationTime(LocalTime.of(17, 0))
                .displayTime("5.00 PM")
                .startDate(LocalDate.of(2026, 5, 30))
                .timezone("Africa/Lagos")
                .notes("After dinner")
                .reminderEnabled(true)
                .reminderText("We will remind you at 5.00 PM every day")
                .takenToday(takenToday)
                .reminders(List.of(
                        MedicationReminderResponse.builder()
                                .offset(MedicationReminderOffset.ON_TIME)
                                .remindAt(OffsetDateTime.parse("2026-05-30T17:00:00+01:00"))
                                .status(MedicationReminderStatus.PENDING)
                                .build()
                ))
                .build();
    }
}
