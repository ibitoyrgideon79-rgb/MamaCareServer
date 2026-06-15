package com.Mamacare.Backend.BabyGrowthPackage.Services;

import com.Mamacare.Backend.BabyGrowthPackage.Dto.BabyGrowthResponse;
import com.Mamacare.Backend.BabyGrowthPackage.Dto.BabyGrowthWeekContent;
import com.Mamacare.Backend.PregnancyCalculationPackage.Dto.PregnancySummaryResponse;
import com.Mamacare.Backend.PregnancyCalculationPackage.Services.PregnancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BabyGrowthService {

  private static final String DEFAULT_AUDIO_LANGUAGE = "YORUBA";
  private static final int AUDIO_DURATION_SECONDS = 75;
  private static final String DISCLAIMER =
      "Baby growth values are educational estimates. Your scan and clinician are the source of truth.";

  private final PregnancyService pregnancyService;
  private final BabyGrowthContentCatalog contentCatalog;

  @Transactional(readOnly = true)
  public BabyGrowthResponse getToday(String userEmail) {
    PregnancySummaryResponse pregnancy = pregnancyService.getMyPregnancy(userEmail);
    BabyGrowthWeekContent content = contentCatalog.getForWeek(pregnancy.week());

    return new BabyGrowthResponse(
        "Baby Growth",
        "Your baby is growing beautifully!",
        new BabyGrowthResponse.AudioUpdate(
            DEFAULT_AUDIO_LANGUAGE,
            "Listen in Yoruba",
            AUDIO_DURATION_SECONDS,
            audioUrlForWeekAndLanguage(pregnancy.week(), DEFAULT_AUDIO_LANGUAGE)
        ),
        new BabyGrowthResponse.PregnancyStatus(
            "You are " + pregnancy.week() + " weeks pregnant",
            formatTrimester(pregnancy.trimester()),
            "Week " + pregnancy.week(),
            pregnancy.week(),
            pregnancy.daysToGo()
        ),
        new BabyGrowthResponse.GrowthThisWeek(
            content.lengthLabel(),
            content.weightLabel(),
            content.heartbeatLabel()
        ),
        content.happenings()
            .stream()
            .map(BabyGrowthResponse.WeeklyHappening::new)
            .toList(),
        DISCLAIMER
    );
  }

  private String audioUrlForWeekAndLanguage(int week, String language) {
    String safeLanguage = language.toLowerCase(Locale.ENGLISH);
    return "/audio/baby-growth/" + safeLanguage + "/week-" + week + ".mp3";
  }

  private String formatTrimester(String trimester) {
    if (trimester == null || trimester.isBlank()) {
      return "";
    }

    String normalized = trimester.trim().toLowerCase(Locale.ENGLISH);

    if (normalized.startsWith("1st")) {
      return "1st Trimester";
    }
    if (normalized.startsWith("2nd")) {
      return "2nd Trimester";
    }
    if (normalized.startsWith("3rd")) {
      return "3rd Trimester";
    }

    return trimester.trim();
  }
}