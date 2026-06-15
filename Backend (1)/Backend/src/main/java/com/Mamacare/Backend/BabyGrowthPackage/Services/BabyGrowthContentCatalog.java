package com.Mamacare.Backend.BabyGrowthPackage.Services;

import com.Mamacare.Backend.BabyGrowthPackage.Dto.BabyGrowthWeekContent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BabyGrowthContentCatalog {

  private static final int MIN_WEEK = 1;
  private static final int MAX_WEEK = 42;
  private static final Map<Integer, BabyGrowthWeekContent> CONTENT_BY_WEEK = buildContent();

  public BabyGrowthWeekContent getForWeek(int week) {
    int safeWeek = Math.max(MIN_WEEK, Math.min(MAX_WEEK, week));
    return CONTENT_BY_WEEK.get(safeWeek);
  }

  private static Map<Integer, BabyGrowthWeekContent> buildContent() {
    Map<Integer, BabyGrowthWeekContent> content = new HashMap<>();

    content.put(1, week("Too early to measure", "Too early to measure", "Too early", "Your pregnancy journey is beginning.", List.of(
        "Pregnancy dating starts from the first day of the last menstrual period.",
        "Your body is preparing for ovulation and fertilization.",
        "A pregnancy test may not be positive yet."
    )));
    content.put(2, week("Too early to measure", "Too early to measure", "Too early", "Your body is preparing for a new beginning.", List.of(
        "Ovulation may happen around this time depending on your cycle.",
        "Fertilization may happen near the end of this week.",
        "It is still too early to measure baby growth."
    )));
    content.put(3, week("< 0.1 cm", "< 1 g", "Too early", "Tiny changes are starting.", List.of(
        "The fertilized egg may be moving toward the uterus.",
        "Cell division is happening quickly.",
        "Implantation may begin soon."
    )));
    content.put(4, week("< 0.1 cm", "< 1 g", "Too early", "A tiny cluster of cells is growing.", List.of(
        "Implantation may be happening.",
        "The pregnancy hormone may start rising.",
        "The placenta is beginning early development."
    )));
    content.put(5, week("0.2 cm", "< 1 g", "Developing", "Early development is moving fast.", List.of(
        "The neural tube is forming.",
        "The heart and blood vessels are beginning early development.",
        "The placenta continues to develop."
    )));
    content.put(6, week("0.4 cm", "< 1 g", "Developing", "Your baby is very tiny but growing quickly.", List.of(
        "The heart tube is developing.",
        "Early facial features are starting.",
        "Small buds that become arms and legs may appear."
    )));
    content.put(7, week("1.0 cm", "< 1 g", "Developing", "Your baby is growing day by day.", List.of(
        "The brain and face are developing.",
        "Arm and leg buds are growing.",
        "The heart is becoming more organized."
    )));
    content.put(8, week("1.6 cm", "1 g", "Developing", "Tiny features are becoming clearer.", List.of(
        "Fingers and toes are beginning to form.",
        "The eyes and ears continue developing.",
        "The heart is beating in a more regular rhythm."
    )));
    content.put(9, week("2.3 cm", "2 g", "Developing", "Your baby is now entering the fetal stage.", List.of(
        "The basic body structure is forming.",
        "Tiny muscles are developing.",
        "The head is still large compared with the body."
    )));
    content.put(10, week("3.1 cm", "4 g", "Developing", "Your baby is growing into a clearer shape.", List.of(
        "Tiny fingers and toes are separating.",
        "The eyelids are developing.",
        "The heart continues to beat steadily."
    )));
    content.put(11, week("4.1 cm", "7 g", "Detectable", "Your baby is becoming more active.", List.of(
        "The baby may open and close tiny fists.",
        "Bones are starting to harden.",
        "The heartbeat may be heard during some clinical checks."
    )));
    content.put(12, week("5.4 cm", "14 g", "Detectable", "Your baby is growing beautifully.", List.of(
        "Reflexes are developing.",
        "The face is becoming more defined.",
        "The digestive system continues developing."
    )));
    content.put(13, week("7.4 cm", "23 g", "Detectable", "Your baby is growing steadily.", List.of(
        "Bones are starting to harden.",
        "The skin is thin and delicate.",
        "The body is starting to catch up with the head."
    )));
    content.put(14, week("8.7 cm", "45 g", "Detectable", "Your baby is entering a new growth phase.", List.of(
        "The neck is becoming more defined.",
        "Red blood cells are forming.",
        "Facial features continue to develop."
    )));
    content.put(15, week("10.1 cm", "70 g", "Detectable", "Your baby is growing quickly.", List.of(
        "Bone development continues.",
        "The scalp hair pattern is forming.",
        "Tiny movements may be happening."
    )));
    content.put(16, week("12.0 cm", "110 g", "Detectable", "Your baby's movement is becoming more coordinated.", List.of(
        "The head is more upright.",
        "The eyes can move slowly.",
        "Limb movements can be seen on ultrasound."
    )));
    content.put(17, week("13.0 cm", "140 g", "Detectable", "Your baby is becoming more active.", List.of(
        "Toenails are starting to develop.",
        "The baby may roll and flip.",
        "You may begin noticing small movements soon."
    )));
    content.put(18, week("14.0 cm", "200 g", "Detectable", "Your baby may begin to hear sounds.", List.of(
        "The ears are standing out from the head.",
        "Hearing is beginning to develop.",
        "The digestive system has started working."
    )));
    content.put(19, week("15.3 cm", "240 g", "Detectable", "Your baby is protected by a soft coating.", List.of(
        "Vernix caseosa is forming on the skin.",
        "The baby is releasing urine into amniotic fluid.",
        "Growth continues steadily."
    )));
    content.put(20, week("16.0 cm", "320 g", "Detectable", "You are around the halfway point.", List.of(
        "You may feel baby movements called quickening.",
        "The baby is sleeping and waking regularly.",
        "Sounds or your movement may wake the baby."
    )));
    content.put(21, week("26.7 cm", "360 g", "Detectable", "Your baby's reflexes are growing stronger.", List.of(
        "The baby may be able to suck a thumb.",
        "Fine hair called lanugo helps hold vernix on the skin.",
        "Movement may become more noticeable."
    )));
    content.put(22, week("27.8 cm", "460 g", "Detectable", "Your baby's features are becoming clearer.", List.of(
        "Eyebrows and hair are visible.",
        "Reproductive organs continue developing.",
        "The baby is gaining more body proportion."
    )));
    content.put(23, week("28.9 cm", "500 g", "Strong", "Your baby's lungs are preparing for breathing later.", List.of(
        "Fingerprints and footprints are forming.",
        "The lungs are starting to make surfactant.",
        "Rapid eye movements may begin."
    )));
    content.put(24, week("30.0 cm", "600 g", "Strong", "Your baby is developing beautifully this week.", List.of(
        "Baby's lungs are developing.",
        "Baby can hear your voice.",
        "Baby's movement may be stronger."
    )));
    content.put(25, week("34.6 cm", "660 g", "Strong", "Your baby may respond to familiar sounds.", List.of(
        "The baby may move in response to your voice.",
        "Sleep cycles are becoming more organized.",
        "Rapid eye movement sleep is increasing."
    )));
    content.put(26, week("35.6 cm", "760 g", "Strong", "Your baby's eyes and lashes are developing.", List.of(
        "Eyebrows and eyelashes have formed.",
        "The eyes are developed but may stay closed.",
        "The baby continues gaining weight."
    )));
    content.put(27, week("36.6 cm", "875 g", "Strong", "Your baby is nearing the end of the second trimester.", List.of(
        "The nervous system continues to mature.",
        "The baby is gaining fat.",
        "The skin may begin looking smoother."
    )));
    content.put(28, week("37.6 cm", "1000 g", "Strong", "Your baby is growing into the third trimester.", List.of(
        "The eyes may begin opening.",
        "The brain continues developing quickly.",
        "Movement patterns may become more familiar."
    )));
    content.put(29, week("38.6 cm", "1150 g", "Strong", "Your baby is gaining strength.", List.of(
        "Muscles and lungs continue maturing.",
        "The baby is gaining more body fat.",
        "Kicks and stretches may feel stronger."
    )));
    content.put(30, week("39.9 cm", "1300 g", "Strong", "Your baby is growing steadily.", List.of(
        "The brain is developing rapidly.",
        "The baby may practice breathing movements.",
        "The body continues storing fat."
    )));
    content.put(31, week("41.1 cm", "1500 g", "Strong", "Your baby is getting bigger every week.", List.of(
        "The baby may turn more often.",
        "Sleep and wake patterns may be clearer.",
        "The lungs continue maturing."
    )));
    content.put(32, week("42.4 cm", "1700 g", "Strong", "Your baby is building strength for birth.", List.of(
        "Toenails and fingernails continue growing.",
        "The baby is gaining fat.",
        "Movements may feel more like rolls and stretches."
    )));
    content.put(33, week("43.7 cm", "1900 g", "Strong", "Your baby is continuing to mature.", List.of(
        "The bones are hardening.",
        "The skull remains flexible for birth.",
        "The immune system continues developing."
    )));
    content.put(34, week("45.0 cm", "2100 g", "Strong", "Your baby is getting closer to birth readiness.", List.of(
        "The lungs continue maturing.",
        "The baby keeps gaining weight.",
        "Movement patterns should remain familiar."
    )));
    content.put(35, week("46.2 cm", "2400 g", "Strong", "Your baby is growing beautifully.", List.of(
        "The baby is gaining more fat.",
        "The kidneys are developed.",
        "The liver is continuing to mature."
    )));
    content.put(36, week("47.4 cm", "2600 g", "Strong", "Your baby is almost ready for the outside world.", List.of(
        "The baby may move lower in the pelvis.",
        "The lungs are close to maturity.",
        "The body is still gaining weight."
    )));
    content.put(37, week("48.6 cm", "2900 g", "Strong", "Your baby is considered early term.", List.of(
        "The baby continues practicing breathing movements.",
        "The brain and lungs keep maturing.",
        "The baby keeps gaining weight."
    )));
    content.put(38, week("49.8 cm", "3100 g", "Strong", "Your baby is nearly ready to meet you.", List.of(
        "The organs are ready for life after birth.",
        "The baby continues adding fat.",
        "Movement may feel different because space is tighter."
    )));
    content.put(39, week("50.7 cm", "3300 g", "Strong", "Your baby is full term.", List.of(
        "The chest is more prominent.",
        "The baby continues preparing for birth.",
        "You may notice regular movement patterns."
    )));
    content.put(40, week("51.2 cm", "3400 g", "Strong", "Your baby is ready for birth.", List.of(
        "The due date is an estimate.",
        "The baby continues gaining small amounts of weight.",
        "Keep following your clinician's guidance."
    )));
    content.put(41, week("51.5 cm", "3600 g", "Strong", "Your baby is still being monitored closely.", List.of(
        "Some pregnancies continue beyond the due date.",
        "Your clinician may monitor baby and mother more closely.",
        "Follow your care team's advice."
    )));
    content.put(42, week("51.7 cm", "3700 g", "Strong", "Your care team should guide next steps.", List.of(
        "Pregnancies beyond 42 weeks need close clinical guidance.",
        "Your clinician may discuss delivery planning.",
        "Keep monitoring movement and follow medical advice."
    )));

    return Map.copyOf(content);
  }

  private static BabyGrowthWeekContent week(
      String lengthLabel,
      String weightLabel,
      String heartbeatLabel,
      String heroMessage,
      List<String> happenings
  ) {
    return new BabyGrowthWeekContent(lengthLabel, weightLabel, heartbeatLabel, heroMessage, happenings);
  }
}