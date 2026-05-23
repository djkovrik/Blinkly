package com.sedsoftware.blinkly.compose.ui.extension

import androidx.compose.runtime.Composable
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.achievement10_express_master
import blinkly.shared.compose.generated.resources.achievement10_express_master_description
import blinkly.shared.compose.generated.resources.achievement11_the_artist
import blinkly.shared.compose.generated.resources.achievement11_the_artist_description
import blinkly.shared.compose.generated.resources.achievement12_clockmaker
import blinkly.shared.compose.generated.resources.achievement12_clockmaker_description
import blinkly.shared.compose.generated.resources.achievement13_reverse_myope
import blinkly.shared.compose.generated.resources.achievement13_reverse_myope_description
import blinkly.shared.compose.generated.resources.achievement14_relax_enthusiast
import blinkly.shared.compose.generated.resources.achievement14_relax_enthusiast_description
import blinkly.shared.compose.generated.resources.achievement15_regular_warm_up
import blinkly.shared.compose.generated.resources.achievement15_regular_warm_up_description
import blinkly.shared.compose.generated.resources.achievement16_evening_ritual
import blinkly.shared.compose.generated.resources.achievement16_evening_ritual_description
import blinkly.shared.compose.generated.resources.achievement17_far_sighted
import blinkly.shared.compose.generated.resources.achievement17_far_sighted_description
import blinkly.shared.compose.generated.resources.achievement18_30_exercises
import blinkly.shared.compose.generated.resources.achievement18_30_exercises_description
import blinkly.shared.compose.generated.resources.achievement19_100_exercises
import blinkly.shared.compose.generated.resources.achievement19_100_exercises_description
import blinkly.shared.compose.generated.resources.achievement1_first_spark
import blinkly.shared.compose.generated.resources.achievement1_first_spark_description
import blinkly.shared.compose.generated.resources.achievement20_iron_gaze
import blinkly.shared.compose.generated.resources.achievement20_iron_gaze_description
import blinkly.shared.compose.generated.resources.achievement21_blink_expert
import blinkly.shared.compose.generated.resources.achievement21_blink_expert_description
import blinkly.shared.compose.generated.resources.achievement22_far_sighted_pro
import blinkly.shared.compose.generated.resources.achievement22_far_sighted_pro_description
import blinkly.shared.compose.generated.resources.achievement23_eagle_eye
import blinkly.shared.compose.generated.resources.achievement23_eagle_eye_description
import blinkly.shared.compose.generated.resources.achievement24_palming_master
import blinkly.shared.compose.generated.resources.achievement24_palming_master_description
import blinkly.shared.compose.generated.resources.achievement25_the_all_seeing
import blinkly.shared.compose.generated.resources.achievement25_the_all_seeing_description
import blinkly.shared.compose.generated.resources.achievement26_200_exercises
import blinkly.shared.compose.generated.resources.achievement26_200_exercises_description
import blinkly.shared.compose.generated.resources.achievement27_falcon_eye
import blinkly.shared.compose.generated.resources.achievement27_falcon_eye_description
import blinkly.shared.compose.generated.resources.achievement28_eternal_guardian
import blinkly.shared.compose.generated.resources.achievement28_eternal_guardian_description
import blinkly.shared.compose.generated.resources.achievement29_blink_legend
import blinkly.shared.compose.generated.resources.achievement29_blink_legend_description
import blinkly.shared.compose.generated.resources.achievement2_blink_starter
import blinkly.shared.compose.generated.resources.achievement2_blink_starter_description
import blinkly.shared.compose.generated.resources.achievement30_far_sighted_guru
import blinkly.shared.compose.generated.resources.achievement30_far_sighted_guru_description
import blinkly.shared.compose.generated.resources.achievement31_palming_yogi
import blinkly.shared.compose.generated.resources.achievement31_palming_yogi_description
import blinkly.shared.compose.generated.resources.achievement32_1001_exercises
import blinkly.shared.compose.generated.resources.achievement32_1001_exercises_description
import blinkly.shared.compose.generated.resources.achievement33_encyclopedia_of_sight
import blinkly.shared.compose.generated.resources.achievement33_encyclopedia_of_sight_description
import blinkly.shared.compose.generated.resources.achievement34_master_of_clarity
import blinkly.shared.compose.generated.resources.achievement34_master_of_clarity_description
import blinkly.shared.compose.generated.resources.achievement35_early_bird
import blinkly.shared.compose.generated.resources.achievement35_early_bird_description
import blinkly.shared.compose.generated.resources.achievement36_night_owl
import blinkly.shared.compose.generated.resources.achievement36_night_owl_description
import blinkly.shared.compose.generated.resources.achievement37_multitasker
import blinkly.shared.compose.generated.resources.achievement37_multitasker_description
import blinkly.shared.compose.generated.resources.achievement38_yin_yang
import blinkly.shared.compose.generated.resources.achievement38_yin_yang_description
import blinkly.shared.compose.generated.resources.achievement39_timeless_gaze
import blinkly.shared.compose.generated.resources.achievement39_timeless_gaze_description
import blinkly.shared.compose.generated.resources.achievement3_close_up
import blinkly.shared.compose.generated.resources.achievement3_close_up_description
import blinkly.shared.compose.generated.resources.achievement40_think_tank
import blinkly.shared.compose.generated.resources.achievement40_think_tank_description
import blinkly.shared.compose.generated.resources.achievement4_clockwise
import blinkly.shared.compose.generated.resources.achievement4_clockwise_description
import blinkly.shared.compose.generated.resources.achievement5_evening_newbie
import blinkly.shared.compose.generated.resources.achievement5_evening_newbie_description
import blinkly.shared.compose.generated.resources.achievement6_20_20_20
import blinkly.shared.compose.generated.resources.achievement6_20_20_20_description
import blinkly.shared.compose.generated.resources.achievement7_3_day_streak
import blinkly.shared.compose.generated.resources.achievement7_3_day_streak_description
import blinkly.shared.compose.generated.resources.achievement8_diamond_eye
import blinkly.shared.compose.generated.resources.achievement8_diamond_eye_description
import blinkly.shared.compose.generated.resources.achievement9_blink_master
import blinkly.shared.compose.generated.resources.achievement9_blink_master_description
import blinkly.shared.compose.generated.resources.achievement_level_beginner
import blinkly.shared.compose.generated.resources.achievement_level_expert
import blinkly.shared.compose.generated.resources.achievement_level_hidden
import blinkly.shared.compose.generated.resources.achievement_level_intermediate
import blinkly.shared.compose.generated.resources.achievement_level_pro
import blinkly.shared.compose.generated.resources.block_a
import blinkly.shared.compose.generated.resources.block_a_desc
import blinkly.shared.compose.generated.resources.block_a_title
import blinkly.shared.compose.generated.resources.block_b
import blinkly.shared.compose.generated.resources.block_b_desc
import blinkly.shared.compose.generated.resources.block_b_title
import blinkly.shared.compose.generated.resources.block_c
import blinkly.shared.compose.generated.resources.block_c_desc
import blinkly.shared.compose.generated.resources.block_c_title
import blinkly.shared.compose.generated.resources.cta_afternoon_warm_up
import blinkly.shared.compose.generated.resources.cta_afternoon_warm_up_desc
import blinkly.shared.compose.generated.resources.cta_day_closing
import blinkly.shared.compose.generated.resources.cta_day_closing_desc
import blinkly.shared.compose.generated.resources.cta_evening_relax
import blinkly.shared.compose.generated.resources.cta_evening_relax_desc
import blinkly.shared.compose.generated.resources.cta_morning_warmup
import blinkly.shared.compose.generated.resources.cta_morning_warmup_desc
import blinkly.shared.compose.generated.resources.cta_perfect_day
import blinkly.shared.compose.generated.resources.cta_perfect_day_desc
import blinkly.shared.compose.generated.resources.cta_work_break
import blinkly.shared.compose.generated.resources.cta_work_break_desc
import blinkly.shared.compose.generated.resources.exercise_blink_break
import blinkly.shared.compose.generated.resources.exercise_blink_break_desc
import blinkly.shared.compose.generated.resources.exercise_clock_rolls
import blinkly.shared.compose.generated.resources.exercise_clock_rolls_desc
import blinkly.shared.compose.generated.resources.exercise_diagonal_gazes
import blinkly.shared.compose.generated.resources.exercise_diagonal_gazes_desc
import blinkly.shared.compose.generated.resources.exercise_figure_eight
import blinkly.shared.compose.generated.resources.exercise_figure_eight_desc
import blinkly.shared.compose.generated.resources.exercise_near_far_focus
import blinkly.shared.compose.generated.resources.exercise_near_far_focus_desc
import blinkly.shared.compose.generated.resources.exercise_palming
import blinkly.shared.compose.generated.resources.exercise_palming_desc
import blinkly.shared.compose.generated.resources.exercise_twenty_x3
import blinkly.shared.compose.generated.resources.exercise_twenty_x3_desc
import blinkly.shared.compose.generated.resources.fact_of_the_day
import blinkly.shared.compose.generated.resources.greetings_afternoon
import blinkly.shared.compose.generated.resources.greetings_evening
import blinkly.shared.compose.generated.resources.greetings_morning
import blinkly.shared.compose.generated.resources.highlight_fact31
import blinkly.shared.compose.generated.resources.highlight_fact32
import blinkly.shared.compose.generated.resources.highlight_fact33
import blinkly.shared.compose.generated.resources.highlight_fact34
import blinkly.shared.compose.generated.resources.highlight_fact35
import blinkly.shared.compose.generated.resources.highlight_fact36
import blinkly.shared.compose.generated.resources.highlight_fact37
import blinkly.shared.compose.generated.resources.highlight_fact38
import blinkly.shared.compose.generated.resources.highlight_fact39
import blinkly.shared.compose.generated.resources.highlight_fact40
import blinkly.shared.compose.generated.resources.highlight_fact41
import blinkly.shared.compose.generated.resources.highlight_fact42
import blinkly.shared.compose.generated.resources.highlight_fact43
import blinkly.shared.compose.generated.resources.highlight_fact44
import blinkly.shared.compose.generated.resources.highlight_fact45
import blinkly.shared.compose.generated.resources.highlight_fact46
import blinkly.shared.compose.generated.resources.highlight_fact47
import blinkly.shared.compose.generated.resources.highlight_fact48
import blinkly.shared.compose.generated.resources.highlight_fact49
import blinkly.shared.compose.generated.resources.highlight_fact50
import blinkly.shared.compose.generated.resources.highlight_tip10_description
import blinkly.shared.compose.generated.resources.highlight_tip10_title
import blinkly.shared.compose.generated.resources.highlight_tip11_description
import blinkly.shared.compose.generated.resources.highlight_tip11_title
import blinkly.shared.compose.generated.resources.highlight_tip12_description
import blinkly.shared.compose.generated.resources.highlight_tip12_title
import blinkly.shared.compose.generated.resources.highlight_tip13_description
import blinkly.shared.compose.generated.resources.highlight_tip13_title
import blinkly.shared.compose.generated.resources.highlight_tip14_description
import blinkly.shared.compose.generated.resources.highlight_tip14_title
import blinkly.shared.compose.generated.resources.highlight_tip15_description
import blinkly.shared.compose.generated.resources.highlight_tip15_title
import blinkly.shared.compose.generated.resources.highlight_tip16_description
import blinkly.shared.compose.generated.resources.highlight_tip16_title
import blinkly.shared.compose.generated.resources.highlight_tip17_description
import blinkly.shared.compose.generated.resources.highlight_tip17_title
import blinkly.shared.compose.generated.resources.highlight_tip18_description
import blinkly.shared.compose.generated.resources.highlight_tip18_title
import blinkly.shared.compose.generated.resources.highlight_tip19_description
import blinkly.shared.compose.generated.resources.highlight_tip19_title
import blinkly.shared.compose.generated.resources.highlight_tip1_description
import blinkly.shared.compose.generated.resources.highlight_tip1_title
import blinkly.shared.compose.generated.resources.highlight_tip20_description
import blinkly.shared.compose.generated.resources.highlight_tip20_title
import blinkly.shared.compose.generated.resources.highlight_tip21_description
import blinkly.shared.compose.generated.resources.highlight_tip21_title
import blinkly.shared.compose.generated.resources.highlight_tip22_description
import blinkly.shared.compose.generated.resources.highlight_tip22_title
import blinkly.shared.compose.generated.resources.highlight_tip23_description
import blinkly.shared.compose.generated.resources.highlight_tip23_title
import blinkly.shared.compose.generated.resources.highlight_tip24_description
import blinkly.shared.compose.generated.resources.highlight_tip24_title
import blinkly.shared.compose.generated.resources.highlight_tip25_description
import blinkly.shared.compose.generated.resources.highlight_tip25_title
import blinkly.shared.compose.generated.resources.highlight_tip26_description
import blinkly.shared.compose.generated.resources.highlight_tip26_title
import blinkly.shared.compose.generated.resources.highlight_tip27_description
import blinkly.shared.compose.generated.resources.highlight_tip27_title
import blinkly.shared.compose.generated.resources.highlight_tip28_description
import blinkly.shared.compose.generated.resources.highlight_tip28_title
import blinkly.shared.compose.generated.resources.highlight_tip29_description
import blinkly.shared.compose.generated.resources.highlight_tip29_title
import blinkly.shared.compose.generated.resources.highlight_tip2_description
import blinkly.shared.compose.generated.resources.highlight_tip2_title
import blinkly.shared.compose.generated.resources.highlight_tip30_description
import blinkly.shared.compose.generated.resources.highlight_tip30_title
import blinkly.shared.compose.generated.resources.highlight_tip3_description
import blinkly.shared.compose.generated.resources.highlight_tip3_title
import blinkly.shared.compose.generated.resources.highlight_tip4_description
import blinkly.shared.compose.generated.resources.highlight_tip4_title
import blinkly.shared.compose.generated.resources.highlight_tip5_description
import blinkly.shared.compose.generated.resources.highlight_tip5_title
import blinkly.shared.compose.generated.resources.highlight_tip6_description
import blinkly.shared.compose.generated.resources.highlight_tip6_title
import blinkly.shared.compose.generated.resources.highlight_tip7_description
import blinkly.shared.compose.generated.resources.highlight_tip7_title
import blinkly.shared.compose.generated.resources.highlight_tip8_description
import blinkly.shared.compose.generated.resources.highlight_tip8_title
import blinkly.shared.compose.generated.resources.highlight_tip9_description
import blinkly.shared.compose.generated.resources.highlight_tip9_title
import blinkly.shared.compose.generated.resources.stage_growing
import blinkly.shared.compose.generated.resources.stage_magnificent
import blinkly.shared.compose.generated.resources.stage_mature
import blinkly.shared.compose.generated.resources.stage_small
import blinkly.shared.compose.generated.resources.stage_strong
import blinkly.shared.compose.generated.resources.stage_tiny
import blinkly.shared.compose.generated.resources.stage_young
import blinkly.shared.compose.generated.resources.tree10_quercus_robur_stage1
import blinkly.shared.compose.generated.resources.tree10_quercus_robur_stage2
import blinkly.shared.compose.generated.resources.tree10_quercus_robur_stage3
import blinkly.shared.compose.generated.resources.tree10_quercus_robur_stage4
import blinkly.shared.compose.generated.resources.tree10_quercus_robur_stage5
import blinkly.shared.compose.generated.resources.tree10_quercus_robur_stage6
import blinkly.shared.compose.generated.resources.tree10_quercus_robur_stage7
import blinkly.shared.compose.generated.resources.tree1_fraxinus_excelsior_stage1
import blinkly.shared.compose.generated.resources.tree1_fraxinus_excelsior_stage2
import blinkly.shared.compose.generated.resources.tree1_fraxinus_excelsior_stage3
import blinkly.shared.compose.generated.resources.tree1_fraxinus_excelsior_stage4
import blinkly.shared.compose.generated.resources.tree1_fraxinus_excelsior_stage5
import blinkly.shared.compose.generated.resources.tree1_fraxinus_excelsior_stage6
import blinkly.shared.compose.generated.resources.tree1_fraxinus_excelsior_stage7
import blinkly.shared.compose.generated.resources.tree2_ginkgo_biloba_stage1
import blinkly.shared.compose.generated.resources.tree2_ginkgo_biloba_stage2
import blinkly.shared.compose.generated.resources.tree2_ginkgo_biloba_stage3
import blinkly.shared.compose.generated.resources.tree2_ginkgo_biloba_stage4
import blinkly.shared.compose.generated.resources.tree2_ginkgo_biloba_stage5
import blinkly.shared.compose.generated.resources.tree2_ginkgo_biloba_stage6
import blinkly.shared.compose.generated.resources.tree2_ginkgo_biloba_stage7
import blinkly.shared.compose.generated.resources.tree3_salix_babylonica_stage1
import blinkly.shared.compose.generated.resources.tree3_salix_babylonica_stage2
import blinkly.shared.compose.generated.resources.tree3_salix_babylonica_stage3
import blinkly.shared.compose.generated.resources.tree3_salix_babylonica_stage4
import blinkly.shared.compose.generated.resources.tree3_salix_babylonica_stage5
import blinkly.shared.compose.generated.resources.tree3_salix_babylonica_stage6
import blinkly.shared.compose.generated.resources.tree3_salix_babylonica_stage7
import blinkly.shared.compose.generated.resources.tree4_pinus_stage1
import blinkly.shared.compose.generated.resources.tree4_pinus_stage2
import blinkly.shared.compose.generated.resources.tree4_pinus_stage3
import blinkly.shared.compose.generated.resources.tree4_pinus_stage4
import blinkly.shared.compose.generated.resources.tree4_pinus_stage5
import blinkly.shared.compose.generated.resources.tree4_pinus_stage6
import blinkly.shared.compose.generated.resources.tree4_pinus_stage7
import blinkly.shared.compose.generated.resources.tree5_adansonia_stage1
import blinkly.shared.compose.generated.resources.tree5_adansonia_stage2
import blinkly.shared.compose.generated.resources.tree5_adansonia_stage3
import blinkly.shared.compose.generated.resources.tree5_adansonia_stage4
import blinkly.shared.compose.generated.resources.tree5_adansonia_stage5
import blinkly.shared.compose.generated.resources.tree5_adansonia_stage6
import blinkly.shared.compose.generated.resources.tree5_adansonia_stage7
import blinkly.shared.compose.generated.resources.tree6_mimosa_pudica_stage1
import blinkly.shared.compose.generated.resources.tree6_mimosa_pudica_stage2
import blinkly.shared.compose.generated.resources.tree6_mimosa_pudica_stage3
import blinkly.shared.compose.generated.resources.tree6_mimosa_pudica_stage4
import blinkly.shared.compose.generated.resources.tree6_mimosa_pudica_stage5
import blinkly.shared.compose.generated.resources.tree6_mimosa_pudica_stage6
import blinkly.shared.compose.generated.resources.tree6_mimosa_pudica_stage7
import blinkly.shared.compose.generated.resources.tree7_sequoia_sempervirens_stage1
import blinkly.shared.compose.generated.resources.tree7_sequoia_sempervirens_stage2
import blinkly.shared.compose.generated.resources.tree7_sequoia_sempervirens_stage3
import blinkly.shared.compose.generated.resources.tree7_sequoia_sempervirens_stage4
import blinkly.shared.compose.generated.resources.tree7_sequoia_sempervirens_stage5
import blinkly.shared.compose.generated.resources.tree7_sequoia_sempervirens_stage6
import blinkly.shared.compose.generated.resources.tree7_sequoia_sempervirens_stage7
import blinkly.shared.compose.generated.resources.tree8_betula_stage1
import blinkly.shared.compose.generated.resources.tree8_betula_stage2
import blinkly.shared.compose.generated.resources.tree8_betula_stage3
import blinkly.shared.compose.generated.resources.tree8_betula_stage4
import blinkly.shared.compose.generated.resources.tree8_betula_stage5
import blinkly.shared.compose.generated.resources.tree8_betula_stage6
import blinkly.shared.compose.generated.resources.tree8_betula_stage7
import blinkly.shared.compose.generated.resources.tree9_ficus_benjamina_stage1
import blinkly.shared.compose.generated.resources.tree9_ficus_benjamina_stage2
import blinkly.shared.compose.generated.resources.tree9_ficus_benjamina_stage3
import blinkly.shared.compose.generated.resources.tree9_ficus_benjamina_stage4
import blinkly.shared.compose.generated.resources.tree9_ficus_benjamina_stage5
import blinkly.shared.compose.generated.resources.tree9_ficus_benjamina_stage6
import blinkly.shared.compose.generated.resources.tree9_ficus_benjamina_stage7
import blinkly.shared.compose.generated.resources.tree_adansonia
import blinkly.shared.compose.generated.resources.tree_betula
import blinkly.shared.compose.generated.resources.tree_ficus_benjamina
import blinkly.shared.compose.generated.resources.tree_fraxinus_excelsior
import blinkly.shared.compose.generated.resources.tree_ginkgo_biloba
import blinkly.shared.compose.generated.resources.tree_mimosa_pudica
import blinkly.shared.compose.generated.resources.tree_pinus
import blinkly.shared.compose.generated.resources.tree_quercus_robur
import blinkly.shared.compose.generated.resources.tree_salix_babylonica
import blinkly.shared.compose.generated.resources.tree_sequoia_sempervirens
import blinkly.shared.compose.generated.resources.tip_of_the_day
import blinkly.shared.compose.generated.resources.week_monday
import blinkly.shared.compose.generated.resources.week_saturday
import blinkly.shared.compose.generated.resources.week_sunday
import blinkly.shared.compose.generated.resources.week_thursday
import blinkly.shared.compose.generated.resources.week_tuesday
import blinkly.shared.compose.generated.resources.week_wednesday
import com.sedsoftware.blinkly.component.main.domain.model.GreetingPeriod
import com.sedsoftware.blinkly.component.main.domain.model.MainCtaState
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayOfWeek.asLabel(): String =
    when (this) {
        DayOfWeek.MONDAY -> stringResource(Res.string.week_monday)
        DayOfWeek.TUESDAY -> stringResource(Res.string.week_tuesday)
        DayOfWeek.WEDNESDAY -> stringResource(Res.string.week_wednesday)
        DayOfWeek.THURSDAY -> stringResource(Res.string.week_thursday)
        DayOfWeek.FRIDAY -> stringResource(Res.string.week_monday)
        DayOfWeek.SATURDAY -> stringResource(Res.string.week_saturday)
        DayOfWeek.SUNDAY -> stringResource(Res.string.week_sunday)
    }

@Composable
@Suppress("CyclomaticComplexMethod")
fun AchievementType.asImage(): DrawableResource =
    when (this) {
        AchievementType.FIRST_SPARK -> Res.drawable.achievement1_first_spark
        AchievementType.BLINK_STARTER -> Res.drawable.achievement2_blink_starter
        AchievementType.CLOSE_UP -> Res.drawable.achievement3_close_up
        AchievementType.CLOCKWISE -> Res.drawable.achievement4_clockwise
        AchievementType.EVENING_NEWBIE -> Res.drawable.achievement5_evening_newbie
        AchievementType.TWENTY_X3 -> Res.drawable.achievement6_20_20_20
        AchievementType.THREE_DAY_STREAK -> Res.drawable.achievement7_3_day_streak
        AchievementType.DIAMOND_EYES -> Res.drawable.achievement8_diamond_eye
        AchievementType.BLINK_MASTER -> Res.drawable.achievement9_blink_master
        AchievementType.EXPRESS_MASTER -> Res.drawable.achievement10_express_master
        AchievementType.THE_ARTIST -> Res.drawable.achievement11_the_artist
        AchievementType.CLOCKMAKER -> Res.drawable.achievement12_clockmaker
        AchievementType.REVERSE_MYOPE -> Res.drawable.achievement13_reverse_myope
        AchievementType.RELAX_ENTHUSIAST -> Res.drawable.achievement14_relax_enthusiast
        AchievementType.REGULAR_WARM_UP -> Res.drawable.achievement15_regular_warm_up
        AchievementType.EVENING_RITUAL -> Res.drawable.achievement16_evening_ritual
        AchievementType.FAR_SIGHTED -> Res.drawable.achievement17_far_sighted
        AchievementType.THIRTY_EXERCISES -> Res.drawable.achievement18_30_exercises
        AchievementType.HUNDRED_EXERCISES -> Res.drawable.achievement19_100_exercises
        AchievementType.IRON_GAZE -> Res.drawable.achievement20_iron_gaze
        AchievementType.BLINK_EXPERT -> Res.drawable.achievement21_blink_expert
        AchievementType.FAR_SIGHTED_PRO -> Res.drawable.achievement22_far_sighted_pro
        AchievementType.EAGLE_EYE -> Res.drawable.achievement23_eagle_eye
        AchievementType.PALMING_MASTER -> Res.drawable.achievement24_palming_master
        AchievementType.THE_ALL_SEEING -> Res.drawable.achievement25_the_all_seeing
        AchievementType.TWO_HUNDRED_EXERCISES -> Res.drawable.achievement26_200_exercises
        AchievementType.FALCON_EYE -> Res.drawable.achievement27_falcon_eye
        AchievementType.ETERNAL_GUARDIAN -> Res.drawable.achievement28_eternal_guardian
        AchievementType.BLINK_LEGEND -> Res.drawable.achievement29_blink_legend
        AchievementType.FAR_SIGHTED_GURU -> Res.drawable.achievement30_far_sighted_guru
        AchievementType.PALMING_YOGI -> Res.drawable.achievement31_palming_yogi
        AchievementType.THOUSAND_AND_ONE -> Res.drawable.achievement32_1001_exercises
        AchievementType.ENCYCLOPEDIA_OF_SIGHT -> Res.drawable.achievement33_encyclopedia_of_sight
        AchievementType.MASTER_OF_CLARITY -> Res.drawable.achievement34_master_of_clarity
        AchievementType.EARLY_BIRD -> Res.drawable.achievement35_early_bird
        AchievementType.NIGHT_OWL -> Res.drawable.achievement36_night_owl
        AchievementType.MULTITASKER -> Res.drawable.achievement37_multitasker
        AchievementType.YIN_YANG -> Res.drawable.achievement38_yin_yang
        AchievementType.TIMELESS_GAZE -> Res.drawable.achievement39_timeless_gaze
        AchievementType.THINK_TANK -> Res.drawable.achievement40_think_tank
    }

@Composable
@Suppress("CyclomaticComplexMethod")
fun AchievementType.asTitle(): String =
    when (this) {
        AchievementType.FIRST_SPARK -> stringResource(Res.string.achievement1_first_spark)
        AchievementType.BLINK_STARTER -> stringResource(Res.string.achievement2_blink_starter)
        AchievementType.CLOSE_UP -> stringResource(Res.string.achievement3_close_up)
        AchievementType.CLOCKWISE -> stringResource(Res.string.achievement4_clockwise)
        AchievementType.EVENING_NEWBIE -> stringResource(Res.string.achievement5_evening_newbie)
        AchievementType.TWENTY_X3 -> stringResource(Res.string.achievement6_20_20_20)
        AchievementType.THREE_DAY_STREAK -> stringResource(Res.string.achievement7_3_day_streak)
        AchievementType.DIAMOND_EYES -> stringResource(Res.string.achievement8_diamond_eye)
        AchievementType.BLINK_MASTER -> stringResource(Res.string.achievement9_blink_master)
        AchievementType.EXPRESS_MASTER -> stringResource(Res.string.achievement10_express_master)
        AchievementType.THE_ARTIST -> stringResource(Res.string.achievement11_the_artist)
        AchievementType.CLOCKMAKER -> stringResource(Res.string.achievement12_clockmaker)
        AchievementType.REVERSE_MYOPE -> stringResource(Res.string.achievement13_reverse_myope)
        AchievementType.RELAX_ENTHUSIAST -> stringResource(Res.string.achievement14_relax_enthusiast)
        AchievementType.REGULAR_WARM_UP -> stringResource(Res.string.achievement15_regular_warm_up)
        AchievementType.EVENING_RITUAL -> stringResource(Res.string.achievement16_evening_ritual)
        AchievementType.FAR_SIGHTED -> stringResource(Res.string.achievement17_far_sighted)
        AchievementType.THIRTY_EXERCISES -> stringResource(Res.string.achievement18_30_exercises)
        AchievementType.HUNDRED_EXERCISES -> stringResource(Res.string.achievement19_100_exercises)
        AchievementType.IRON_GAZE -> stringResource(Res.string.achievement20_iron_gaze)
        AchievementType.BLINK_EXPERT -> stringResource(Res.string.achievement21_blink_expert)
        AchievementType.FAR_SIGHTED_PRO -> stringResource(Res.string.achievement22_far_sighted_pro)
        AchievementType.EAGLE_EYE -> stringResource(Res.string.achievement23_eagle_eye)
        AchievementType.PALMING_MASTER -> stringResource(Res.string.achievement24_palming_master)
        AchievementType.THE_ALL_SEEING -> stringResource(Res.string.achievement25_the_all_seeing)
        AchievementType.TWO_HUNDRED_EXERCISES -> stringResource(Res.string.achievement26_200_exercises)
        AchievementType.FALCON_EYE -> stringResource(Res.string.achievement27_falcon_eye)
        AchievementType.ETERNAL_GUARDIAN -> stringResource(Res.string.achievement28_eternal_guardian)
        AchievementType.BLINK_LEGEND -> stringResource(Res.string.achievement29_blink_legend)
        AchievementType.FAR_SIGHTED_GURU -> stringResource(Res.string.achievement30_far_sighted_guru)
        AchievementType.PALMING_YOGI -> stringResource(Res.string.achievement31_palming_yogi)
        AchievementType.THOUSAND_AND_ONE -> stringResource(Res.string.achievement32_1001_exercises)
        AchievementType.ENCYCLOPEDIA_OF_SIGHT -> stringResource(Res.string.achievement33_encyclopedia_of_sight)
        AchievementType.MASTER_OF_CLARITY -> stringResource(Res.string.achievement34_master_of_clarity)
        AchievementType.EARLY_BIRD -> stringResource(Res.string.achievement35_early_bird)
        AchievementType.NIGHT_OWL -> stringResource(Res.string.achievement36_night_owl)
        AchievementType.MULTITASKER -> stringResource(Res.string.achievement37_multitasker)
        AchievementType.YIN_YANG -> stringResource(Res.string.achievement38_yin_yang)
        AchievementType.TIMELESS_GAZE -> stringResource(Res.string.achievement39_timeless_gaze)
        AchievementType.THINK_TANK -> stringResource(Res.string.achievement40_think_tank)
    }

@Composable
@Suppress("CyclomaticComplexMethod")
fun AchievementType.asDescription(): String =
    when (this) {
        AchievementType.FIRST_SPARK -> stringResource(Res.string.achievement1_first_spark_description)
        AchievementType.BLINK_STARTER -> stringResource(Res.string.achievement2_blink_starter_description)
        AchievementType.CLOSE_UP -> stringResource(Res.string.achievement3_close_up_description)
        AchievementType.CLOCKWISE -> stringResource(Res.string.achievement4_clockwise_description)
        AchievementType.EVENING_NEWBIE -> stringResource(Res.string.achievement5_evening_newbie_description)
        AchievementType.TWENTY_X3 -> stringResource(Res.string.achievement6_20_20_20_description)
        AchievementType.THREE_DAY_STREAK -> stringResource(Res.string.achievement7_3_day_streak_description)
        AchievementType.DIAMOND_EYES -> stringResource(Res.string.achievement8_diamond_eye_description)
        AchievementType.BLINK_MASTER -> stringResource(Res.string.achievement9_blink_master_description)
        AchievementType.EXPRESS_MASTER -> stringResource(Res.string.achievement10_express_master_description)
        AchievementType.THE_ARTIST -> stringResource(Res.string.achievement11_the_artist_description)
        AchievementType.CLOCKMAKER -> stringResource(Res.string.achievement12_clockmaker_description)
        AchievementType.REVERSE_MYOPE -> stringResource(Res.string.achievement13_reverse_myope_description)
        AchievementType.RELAX_ENTHUSIAST -> stringResource(Res.string.achievement14_relax_enthusiast_description)
        AchievementType.REGULAR_WARM_UP -> stringResource(Res.string.achievement15_regular_warm_up_description)
        AchievementType.EVENING_RITUAL -> stringResource(Res.string.achievement16_evening_ritual_description)
        AchievementType.FAR_SIGHTED -> stringResource(Res.string.achievement17_far_sighted_description)
        AchievementType.THIRTY_EXERCISES -> stringResource(Res.string.achievement18_30_exercises_description)
        AchievementType.HUNDRED_EXERCISES -> stringResource(Res.string.achievement19_100_exercises_description)
        AchievementType.IRON_GAZE -> stringResource(Res.string.achievement20_iron_gaze_description)
        AchievementType.BLINK_EXPERT -> stringResource(Res.string.achievement21_blink_expert_description)
        AchievementType.FAR_SIGHTED_PRO -> stringResource(Res.string.achievement22_far_sighted_pro_description)
        AchievementType.EAGLE_EYE -> stringResource(Res.string.achievement23_eagle_eye_description)
        AchievementType.PALMING_MASTER -> stringResource(Res.string.achievement24_palming_master_description)
        AchievementType.THE_ALL_SEEING -> stringResource(Res.string.achievement25_the_all_seeing_description)
        AchievementType.TWO_HUNDRED_EXERCISES -> stringResource(Res.string.achievement26_200_exercises_description)
        AchievementType.FALCON_EYE -> stringResource(Res.string.achievement27_falcon_eye_description)
        AchievementType.ETERNAL_GUARDIAN -> stringResource(Res.string.achievement28_eternal_guardian_description)
        AchievementType.BLINK_LEGEND -> stringResource(Res.string.achievement29_blink_legend_description)
        AchievementType.FAR_SIGHTED_GURU -> stringResource(Res.string.achievement30_far_sighted_guru_description)
        AchievementType.PALMING_YOGI -> stringResource(Res.string.achievement31_palming_yogi_description)
        AchievementType.THOUSAND_AND_ONE -> stringResource(Res.string.achievement32_1001_exercises_description)
        AchievementType.ENCYCLOPEDIA_OF_SIGHT -> stringResource(Res.string.achievement33_encyclopedia_of_sight_description)
        AchievementType.MASTER_OF_CLARITY -> stringResource(Res.string.achievement34_master_of_clarity_description)
        AchievementType.EARLY_BIRD -> stringResource(Res.string.achievement35_early_bird_description)
        AchievementType.NIGHT_OWL -> stringResource(Res.string.achievement36_night_owl_description)
        AchievementType.MULTITASKER -> stringResource(Res.string.achievement37_multitasker_description)
        AchievementType.YIN_YANG -> stringResource(Res.string.achievement38_yin_yang_description)
        AchievementType.TIMELESS_GAZE -> stringResource(Res.string.achievement39_timeless_gaze_description)
        AchievementType.THINK_TANK -> stringResource(Res.string.achievement40_think_tank_description)
    }

@Composable
fun AchievementLevel.asLabel(): String =
    when (this) {
        AchievementLevel.BEGINNER -> stringResource(Res.string.achievement_level_beginner)
        AchievementLevel.INTERMEDIATE -> stringResource(Res.string.achievement_level_intermediate)
        AchievementLevel.PRO -> stringResource(Res.string.achievement_level_pro)
        AchievementLevel.EXPERT -> stringResource(Res.string.achievement_level_expert)
        AchievementLevel.HIDDEN -> stringResource(Res.string.achievement_level_hidden)
    }

@Composable
fun TreeType.asLabel(): String =
    when (this) {
        TreeType.FRAXINUS_EXCELSIOR -> stringResource(Res.string.tree_fraxinus_excelsior)
        TreeType.GINKGO_BILOBA -> stringResource(Res.string.tree_ginkgo_biloba)
        TreeType.SALIX_BABYLONICA -> stringResource(Res.string.tree_salix_babylonica)
        TreeType.PINUS -> stringResource(Res.string.tree_pinus)
        TreeType.ADANSONIA -> stringResource(Res.string.tree_adansonia)
        TreeType.MIMOSA_PUDICA -> stringResource(Res.string.tree_mimosa_pudica)
        TreeType.SEQUOIA_SEMPERVIRENS -> stringResource(Res.string.tree_sequoia_sempervirens)
        TreeType.BETULA -> stringResource(Res.string.tree_betula)
        TreeType.FICUS_BENJAMINA -> stringResource(Res.string.tree_ficus_benjamina)
        TreeType.QUERCUS_ROBUR -> stringResource(Res.string.tree_quercus_robur)
    }

@Composable
fun TreeStage.asLabel(): String =
    when (this) {
        TreeStage.TINY -> stringResource(Res.string.stage_tiny)
        TreeStage.SMALL -> stringResource(Res.string.stage_small)
        TreeStage.YOUNG -> stringResource(Res.string.stage_young)
        TreeStage.GROWING -> stringResource(Res.string.stage_growing)
        TreeStage.STRONG -> stringResource(Res.string.stage_strong)
        TreeStage.MATURE -> stringResource(Res.string.stage_mature)
        TreeStage.MAGNIFICENT -> stringResource(Res.string.stage_magnificent)
    }

@Composable
fun GreetingPeriod.asLabel(): String =
    when (this) {
        GreetingPeriod.MORNING -> stringResource(Res.string.greetings_morning)
        GreetingPeriod.DAY -> stringResource(Res.string.greetings_afternoon)
        GreetingPeriod.EVENING -> stringResource(Res.string.greetings_evening)
    }

@Composable
fun MainCtaState.asTitle(): String =
    when (this) {
        MainCtaState.MorningWarmUp,
        MainCtaState.Idle -> stringResource(Res.string.cta_morning_warmup)
        MainCtaState.WorkBreakDue -> stringResource(Res.string.cta_work_break)
        MainCtaState.AfternoonWarmUp -> stringResource(Res.string.cta_afternoon_warm_up)
        MainCtaState.EveningRelax -> stringResource(Res.string.cta_evening_relax)
        MainCtaState.DayClosing -> stringResource(Res.string.cta_day_closing)
        MainCtaState.PerfectDay -> stringResource(Res.string.cta_perfect_day)
    }

@Composable
fun MainCtaState.asDescription(): String =
    when (this) {
        MainCtaState.MorningWarmUp,
        MainCtaState.Idle -> stringResource(Res.string.cta_morning_warmup_desc)
        MainCtaState.WorkBreakDue -> stringResource(Res.string.cta_work_break_desc)
        MainCtaState.AfternoonWarmUp -> stringResource(Res.string.cta_afternoon_warm_up_desc)
        MainCtaState.EveningRelax -> stringResource(Res.string.cta_evening_relax_desc)
        MainCtaState.DayClosing -> stringResource(Res.string.cta_day_closing_desc)
        MainCtaState.PerfectDay -> stringResource(Res.string.cta_perfect_day_desc)
    }

@Composable
fun ExerciseBlock.asLabel(): String =
    when(this) {
        ExerciseBlock.A -> stringResource(Res.string.block_a)
        ExerciseBlock.B -> stringResource(Res.string.block_b)
        ExerciseBlock.C -> stringResource(Res.string.block_c)
    }

@Composable
fun ExerciseBlock.asTitle(): String =
    when(this) {
        ExerciseBlock.A -> stringResource(Res.string.block_a_title)
        ExerciseBlock.B -> stringResource(Res.string.block_b_title)
        ExerciseBlock.C -> stringResource(Res.string.block_c_title)
    }

@Composable
fun ExerciseBlock.asDescription(): String =
    when(this) {
        ExerciseBlock.A -> stringResource(Res.string.block_a_desc)
        ExerciseBlock.B -> stringResource(Res.string.block_b_desc)
        ExerciseBlock.C -> stringResource(Res.string.block_c_desc)
    }

@Composable
fun ExerciseType.asTitle(): String =
    when (this) {
        ExerciseType.BLINK_BREAK -> stringResource(Res.string.exercise_blink_break)
        ExerciseType.NEAR_FAR_FOCUS -> stringResource(Res.string.exercise_near_far_focus)
        ExerciseType.DIAGONAL_GAZES -> stringResource(Res.string.exercise_diagonal_gazes)
        ExerciseType.FIGURE_EIGHT -> stringResource(Res.string.exercise_figure_eight)
        ExerciseType.CLOCK_ROLLS -> stringResource(Res.string.exercise_clock_rolls)
        ExerciseType.PALMING -> stringResource(Res.string.exercise_palming)
        ExerciseType.TWENTY_X3 -> stringResource(Res.string.exercise_twenty_x3)
    }

@Composable
fun ExerciseType.asDescription(): String =
    when (this) {
        ExerciseType.BLINK_BREAK -> stringResource(Res.string.exercise_blink_break_desc)
        ExerciseType.NEAR_FAR_FOCUS -> stringResource(Res.string.exercise_near_far_focus_desc)
        ExerciseType.DIAGONAL_GAZES -> stringResource(Res.string.exercise_diagonal_gazes_desc)
        ExerciseType.FIGURE_EIGHT -> stringResource(Res.string.exercise_figure_eight_desc)
        ExerciseType.CLOCK_ROLLS -> stringResource(Res.string.exercise_clock_rolls_desc)
        ExerciseType.PALMING -> stringResource(Res.string.exercise_palming_desc)
        ExerciseType.TWENTY_X3 -> stringResource(Res.string.exercise_twenty_x3_desc)
    }

@Composable
fun HighlightOfTheDay.asTitle(): String =
    when (this) {
        is HighlightOfTheDay.Tip -> stringResource(Res.string.tip_of_the_day)
        is HighlightOfTheDay.Fact -> stringResource(Res.string.fact_of_the_day)
    }

@Composable
fun HighlightOfTheDay.Tip.asTitle(): String =
    stringResource(titleResource())

@Composable
fun HighlightOfTheDay.Tip.asDescription(): String =
    stringResource(descriptionResource())

@Composable
fun HighlightOfTheDay.Fact.asDescription(): String =
    stringResource(descriptionResource())

private fun HighlightOfTheDay.Tip.titleResource(): StringResource =
    when (index) {
        1 -> Res.string.highlight_tip1_title
        2 -> Res.string.highlight_tip2_title
        3 -> Res.string.highlight_tip3_title
        4 -> Res.string.highlight_tip4_title
        5 -> Res.string.highlight_tip5_title
        6 -> Res.string.highlight_tip6_title
        7 -> Res.string.highlight_tip7_title
        8 -> Res.string.highlight_tip8_title
        9 -> Res.string.highlight_tip9_title
        10 -> Res.string.highlight_tip10_title
        11 -> Res.string.highlight_tip11_title
        12 -> Res.string.highlight_tip12_title
        13 -> Res.string.highlight_tip13_title
        14 -> Res.string.highlight_tip14_title
        15 -> Res.string.highlight_tip15_title
        16 -> Res.string.highlight_tip16_title
        17 -> Res.string.highlight_tip17_title
        18 -> Res.string.highlight_tip18_title
        19 -> Res.string.highlight_tip19_title
        20 -> Res.string.highlight_tip20_title
        21 -> Res.string.highlight_tip21_title
        22 -> Res.string.highlight_tip22_title
        23 -> Res.string.highlight_tip23_title
        24 -> Res.string.highlight_tip24_title
        25 -> Res.string.highlight_tip25_title
        26 -> Res.string.highlight_tip26_title
        27 -> Res.string.highlight_tip27_title
        28 -> Res.string.highlight_tip28_title
        29 -> Res.string.highlight_tip29_title
        30 -> Res.string.highlight_tip30_title
        else -> Res.string.highlight_tip1_title
    }

private fun HighlightOfTheDay.Tip.descriptionResource(): StringResource =
    when (index) {
        1 -> Res.string.highlight_tip1_description
        2 -> Res.string.highlight_tip2_description
        3 -> Res.string.highlight_tip3_description
        4 -> Res.string.highlight_tip4_description
        5 -> Res.string.highlight_tip5_description
        6 -> Res.string.highlight_tip6_description
        7 -> Res.string.highlight_tip7_description
        8 -> Res.string.highlight_tip8_description
        9 -> Res.string.highlight_tip9_description
        10 -> Res.string.highlight_tip10_description
        11 -> Res.string.highlight_tip11_description
        12 -> Res.string.highlight_tip12_description
        13 -> Res.string.highlight_tip13_description
        14 -> Res.string.highlight_tip14_description
        15 -> Res.string.highlight_tip15_description
        16 -> Res.string.highlight_tip16_description
        17 -> Res.string.highlight_tip17_description
        18 -> Res.string.highlight_tip18_description
        19 -> Res.string.highlight_tip19_description
        20 -> Res.string.highlight_tip20_description
        21 -> Res.string.highlight_tip21_description
        22 -> Res.string.highlight_tip22_description
        23 -> Res.string.highlight_tip23_description
        24 -> Res.string.highlight_tip24_description
        25 -> Res.string.highlight_tip25_description
        26 -> Res.string.highlight_tip26_description
        27 -> Res.string.highlight_tip27_description
        28 -> Res.string.highlight_tip28_description
        29 -> Res.string.highlight_tip29_description
        30 -> Res.string.highlight_tip30_description
        else -> Res.string.highlight_tip1_description
    }

private fun HighlightOfTheDay.Fact.descriptionResource(): StringResource =
    when (index) {
        31 -> Res.string.highlight_fact31
        32 -> Res.string.highlight_fact32
        33 -> Res.string.highlight_fact33
        34 -> Res.string.highlight_fact34
        35 -> Res.string.highlight_fact35
        36 -> Res.string.highlight_fact36
        37 -> Res.string.highlight_fact37
        38 -> Res.string.highlight_fact38
        39 -> Res.string.highlight_fact39
        40 -> Res.string.highlight_fact40
        41 -> Res.string.highlight_fact41
        42 -> Res.string.highlight_fact42
        43 -> Res.string.highlight_fact43
        44 -> Res.string.highlight_fact44
        45 -> Res.string.highlight_fact45
        46 -> Res.string.highlight_fact46
        47 -> Res.string.highlight_fact47
        48 -> Res.string.highlight_fact48
        49 -> Res.string.highlight_fact49
        50 -> Res.string.highlight_fact50
        else -> Res.string.highlight_fact31
    }

@Composable
@Suppress("CyclomaticComplexMethod")
fun Tree.asImage(): DrawableResource =
    when (type) {
        TreeType.FRAXINUS_EXCELSIOR ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree1_fraxinus_excelsior_stage1
                TreeStage.SMALL -> Res.drawable.tree1_fraxinus_excelsior_stage2
                TreeStage.YOUNG -> Res.drawable.tree1_fraxinus_excelsior_stage3
                TreeStage.GROWING -> Res.drawable.tree1_fraxinus_excelsior_stage4
                TreeStage.STRONG -> Res.drawable.tree1_fraxinus_excelsior_stage5
                TreeStage.MATURE -> Res.drawable.tree1_fraxinus_excelsior_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree1_fraxinus_excelsior_stage7
            }

        TreeType.GINKGO_BILOBA ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree2_ginkgo_biloba_stage1
                TreeStage.SMALL -> Res.drawable.tree2_ginkgo_biloba_stage2
                TreeStage.YOUNG -> Res.drawable.tree2_ginkgo_biloba_stage3
                TreeStage.GROWING -> Res.drawable.tree2_ginkgo_biloba_stage4
                TreeStage.STRONG -> Res.drawable.tree2_ginkgo_biloba_stage5
                TreeStage.MATURE -> Res.drawable.tree2_ginkgo_biloba_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree2_ginkgo_biloba_stage7
            }

        TreeType.SALIX_BABYLONICA ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree3_salix_babylonica_stage1
                TreeStage.SMALL -> Res.drawable.tree3_salix_babylonica_stage2
                TreeStage.YOUNG -> Res.drawable.tree3_salix_babylonica_stage3
                TreeStage.GROWING -> Res.drawable.tree3_salix_babylonica_stage4
                TreeStage.STRONG -> Res.drawable.tree3_salix_babylonica_stage5
                TreeStage.MATURE -> Res.drawable.tree3_salix_babylonica_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree3_salix_babylonica_stage7
            }

        TreeType.PINUS ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree4_pinus_stage1
                TreeStage.SMALL -> Res.drawable.tree4_pinus_stage2
                TreeStage.YOUNG -> Res.drawable.tree4_pinus_stage3
                TreeStage.GROWING -> Res.drawable.tree4_pinus_stage4
                TreeStage.STRONG -> Res.drawable.tree4_pinus_stage5
                TreeStage.MATURE -> Res.drawable.tree4_pinus_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree4_pinus_stage7
            }

        TreeType.ADANSONIA ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree5_adansonia_stage1
                TreeStage.SMALL -> Res.drawable.tree5_adansonia_stage2
                TreeStage.YOUNG -> Res.drawable.tree5_adansonia_stage3
                TreeStage.GROWING -> Res.drawable.tree5_adansonia_stage4
                TreeStage.STRONG -> Res.drawable.tree5_adansonia_stage5
                TreeStage.MATURE -> Res.drawable.tree5_adansonia_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree5_adansonia_stage7
            }

        TreeType.MIMOSA_PUDICA ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree6_mimosa_pudica_stage1
                TreeStage.SMALL -> Res.drawable.tree6_mimosa_pudica_stage2
                TreeStage.YOUNG -> Res.drawable.tree6_mimosa_pudica_stage3
                TreeStage.GROWING -> Res.drawable.tree6_mimosa_pudica_stage4
                TreeStage.STRONG -> Res.drawable.tree6_mimosa_pudica_stage5
                TreeStage.MATURE -> Res.drawable.tree6_mimosa_pudica_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree6_mimosa_pudica_stage7
            }

        TreeType.SEQUOIA_SEMPERVIRENS ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree7_sequoia_sempervirens_stage1
                TreeStage.SMALL -> Res.drawable.tree7_sequoia_sempervirens_stage2
                TreeStage.YOUNG -> Res.drawable.tree7_sequoia_sempervirens_stage3
                TreeStage.GROWING -> Res.drawable.tree7_sequoia_sempervirens_stage4
                TreeStage.STRONG -> Res.drawable.tree7_sequoia_sempervirens_stage5
                TreeStage.MATURE -> Res.drawable.tree7_sequoia_sempervirens_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree7_sequoia_sempervirens_stage7
            }

        TreeType.BETULA ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree8_betula_stage1
                TreeStage.SMALL -> Res.drawable.tree8_betula_stage2
                TreeStage.YOUNG -> Res.drawable.tree8_betula_stage3
                TreeStage.GROWING -> Res.drawable.tree8_betula_stage4
                TreeStage.STRONG -> Res.drawable.tree8_betula_stage5
                TreeStage.MATURE -> Res.drawable.tree8_betula_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree8_betula_stage7
            }

        TreeType.FICUS_BENJAMINA ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree9_ficus_benjamina_stage1
                TreeStage.SMALL -> Res.drawable.tree9_ficus_benjamina_stage2
                TreeStage.YOUNG -> Res.drawable.tree9_ficus_benjamina_stage3
                TreeStage.GROWING -> Res.drawable.tree9_ficus_benjamina_stage4
                TreeStage.STRONG -> Res.drawable.tree9_ficus_benjamina_stage5
                TreeStage.MATURE -> Res.drawable.tree9_ficus_benjamina_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree9_ficus_benjamina_stage7
            }

        TreeType.QUERCUS_ROBUR ->
            when (stage) {
                TreeStage.TINY -> Res.drawable.tree10_quercus_robur_stage1
                TreeStage.SMALL -> Res.drawable.tree10_quercus_robur_stage2
                TreeStage.YOUNG -> Res.drawable.tree10_quercus_robur_stage3
                TreeStage.GROWING -> Res.drawable.tree10_quercus_robur_stage4
                TreeStage.STRONG -> Res.drawable.tree10_quercus_robur_stage5
                TreeStage.MATURE -> Res.drawable.tree10_quercus_robur_stage6
                TreeStage.MAGNIFICENT -> Res.drawable.tree10_quercus_robur_stage7
            }
    }
