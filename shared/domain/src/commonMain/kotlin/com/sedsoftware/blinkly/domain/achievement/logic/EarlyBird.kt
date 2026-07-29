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

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return calendar.any { workout ->
            workout.exercises.any { exercise ->
                exercise.completedAt.hour(timeZone) < LATEST_EARLY_HOUR
            }
        }
    }

    private companion object {
        const val LATEST_EARLY_HOUR = 8
    }
}
