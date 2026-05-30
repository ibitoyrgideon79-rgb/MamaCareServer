package com.Mamacare.Backend.MedicationRemiderPackage.Service;

import com.Mamacare.Backend.MedicationRemiderPackage.DTOs.MedicationTipResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationTipService {

    public List<MedicationTipResponse> getTips() {
        return List.of(
                MedicationTipResponse.builder()
                        .title("Take with water")
                        .body("Water helps many medicines move through the body safely.")
                        .build(),
                MedicationTipResponse.builder()
                        .title("Keep a steady time")
                        .body("Taking medicine around the same time each day helps build a routine.")
                        .build(),
                MedicationTipResponse.builder()
                        .title("Ask before changing dose")
                        .body("Talk to your doctor or pharmacist before changing how much you take.")
                        .build()
        );
    }
}