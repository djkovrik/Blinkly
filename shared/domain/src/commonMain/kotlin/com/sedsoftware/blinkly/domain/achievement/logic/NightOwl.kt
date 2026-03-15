package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.extension.hour
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.datetime.TimeZone

/**
 * Achievement #36
 * Night Owl - Complete a workout after 11:00 PM
 */
internal class NightOwl(
    private val timeZone: TimeZone,
) : UnlockableAchievement {

    override val type: AchievementType = AchievementType.NIGHT_OWL

    private val requiredHours: Set<Int> = setOf(23, 0, 1)

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return calendar.any { workout ->
            workout.exercises.isNotEmpty() && workout.exercises.all { exercise ->
                val hour = exercise.completedAt.hour(timeZone)
                requiredHours.contains(hour)
            }
        }
    }
}
