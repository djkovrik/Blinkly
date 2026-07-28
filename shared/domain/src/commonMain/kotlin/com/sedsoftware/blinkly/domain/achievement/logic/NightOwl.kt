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

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return calendar.any { workout ->
            workout.exercises.any { exercise ->
                exercise.completedAt.hour(timeZone) >= EARLIEST_LATE_HOUR
            }
        }
    }

    private companion object {
        const val EARLIEST_LATE_HOUR = 23
    }
}
