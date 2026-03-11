package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ExerciseType

/**
 * Achievement #21
 * Blink Expert - Perform 5000 blinks
 */
internal class BlinkExpert(
    private val blinkBreakCount: Int,
) : UnlockableAchievement {

    override val type: AchievementType = AchievementType.BLINK_EXPERT

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val count = calendar.flatMap { it.exercises }.count { it.type == ExerciseType.BLINK_BREAK }
        val result = count * blinkBreakCount
        return result >= ACHIEVEMENT_THRESHOLD
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 5000
    }
}
