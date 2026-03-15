package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.extension.hour
import com.sedsoftware.blinkly.domain.extension.minute
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.datetime.TimeZone

/**
 * Achievement #39
 * Timeless Gaze - Complete an exercise exactly at midnight
 */
internal class TimelessGaze(
    private val timeZone: TimeZone,
) : UnlockableAchievement {

    override val type: AchievementType = AchievementType.TIMELESS_GAZE

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return calendar.any { workout ->
            workout.exercises.isNotEmpty() && workout.exercises.any { exercise ->
                val hour = exercise.completedAt.hour(timeZone)
                val minute = exercise.completedAt.minute(timeZone)
                hour == 0 && minute == 0
            }
        }
    }
}
