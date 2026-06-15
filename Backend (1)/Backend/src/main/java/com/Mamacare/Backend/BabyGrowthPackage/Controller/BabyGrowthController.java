package com.Mamacare.Backend.BabyGrowthPackage.Controller;

import com.Mamacare.Backend.BabyGrowthPackage.Dto.BabyGrowthResponse;
import com.Mamacare.Backend.BabyGrowthPackage.Services.BabyGrowthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/baby-growth")
@RequiredArgsConstructor
public class BabyGrowthController {

  private final BabyGrowthService babyGrowthService;

  @GetMapping("/today")
  public ResponseEntity<BabyGrowthResponse> getToday(Principal principal) {
    return ResponseEntity.ok(babyGrowthService.getToday(principal.getName()));
  }
}