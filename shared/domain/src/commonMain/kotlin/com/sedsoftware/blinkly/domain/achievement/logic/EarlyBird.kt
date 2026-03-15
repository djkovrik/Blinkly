package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.extension.hour
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.datetime.TimeZone

/**
 * Achievement #35
 * Early Bird - Complete a workout before 8:00 AM
 */
internal class EarlyBird(
    private val timeZone: TimeZone,
) : UnlockableAchievement {

    override val type: AchievementType = AchievementType.EARLY_BIRD

    private val requiredHours: Set<Int> = setOf(5, 6, 7)

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return calendar.any { workout ->
            workout.exercises.isNotEmpty() && workout.exercises.all { exercise ->
                val hour = exercise.completedAt.hour(timeZone)
                requiredHours.contains(hour)
            }
        }
    }
}
