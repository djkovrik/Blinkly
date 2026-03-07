package com.sedsoftware.blinkly.domain.extension

import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

fun AchievementType.getLevel(): AchievementLevel =
    when (this) {
        AchievementType.UNKNOWN
            -> AchievementLevel.UNKNOWN

        AchievementType.FIRST_SPARK,
        AchievementType.BLINK_STARTER,
        AchievementType.CLOSE_UP,
        AchievementType.CLOCKWISE,
        AchievementType.EVENING_NEWBIE,
        AchievementType.TWENTY_X3,
        AchievementType.THREE_DAY_STREAK,
            -> AchievementLevel.BEGINNER

        AchievementType.DIAMOND_EYES,
        AchievementType.BLINK_MASTER,
        AchievementType.FAR_SIGHTED,
        AchievementType.THE_ARTIST,
        AchievementType.CLOCKMAKER,
        AchievementType.REVERSE_MYOPE,
        AchievementType.RELAX_ENTHUSIAST,
        AchievementType.REGULAR_WARM_UP,
        AchievementType.EVENING_RITUAL,
        AchievementType.EXPRESS_MASTER,
        AchievementType.THIRTY_EXERCISES,
        AchievementType.HUNDRED_EXERCISES,
            -> AchievementLevel.INTERMEDIATE

        AchievementType.IRON_GAZE,
        AchievementType.BLINK_EXPERT,
        AchievementType.FAR_SIGHTED_PRO,
        AchievementType.EAGLE_EYE,
        AchievementType.PALMING_MASTER,
        AchievementType.THE_ALL_SEEING,
        AchievementType.TWO_HUNDRED_EXERCISES,
            -> AchievementLevel.PRO

        AchievementType.FALCON_EYE,
        AchievementType.ETERNAL_GUARDIAN,
        AchievementType.BLINK_LEGEND,
        AchievementType.FAR_SIGHTED_GURU,
        AchievementType.PALMING_YOGI,
        AchievementType.THOUSAND_AND_ONE,
        AchievementType.ENCYCLOPEDIA_OF_SIGHT,
        AchievementType.MASTER_OF_CLARITY,
            -> AchievementLevel.EXPERT

        AchievementType.EARLY_BIRD,
        AchievementType.NIGHT_OWL,
        AchievementType.MULTITASKER,
        AchievementType.YIN_YANG,
        AchievementType.TIMELESS_GAZE,
        AchievementType.THINK_TANK,
            -> AchievementLevel.HIDDEN
    }

fun List<Workout>.hasNConsecutiveDays(
    n: Int,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Boolean {
    val uniqueDates: List<LocalDate> = this
        .mapNotNull { workout ->
            workout.exercises.firstOrNull()?.completedAt?.toLocalDateTime(timeZone)?.date
        }
        .toSet()
        .sorted()

    if (uniqueDates.size < n) return false

    var consecutiveCount = 1
    for (i in 1 until uniqueDates.size) {
        val prevDate = uniqueDates[i - 1]
        val currDate = uniqueDates[i]

        if (currDate == prevDate.plus(1, DateTimeUnit.DAY)) {
            consecutiveCount++
            if (consecutiveCount >= n) return true
        } else {
            consecutiveCount = 1
        }
    }

    return false
}
