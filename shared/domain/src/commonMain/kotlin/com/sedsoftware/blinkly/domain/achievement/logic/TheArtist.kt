package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ExerciseType

/**
 * Achievement #11
 * The Artist - Draw the "Figure 8" with your eyes 30 times
 */
internal class TheArtist(
    private val figureEightCount: Int,
) : UnlockableAchievement {

    override val type: AchievementType = AchievementType.THE_ARTIST

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val count = calendar.flatMap { it.exercises }.count { it.type == ExerciseType.FIGURE_EIGHT }
        val result = count * figureEightCount
        return result >= ACHIEVEMENT_THRESHOLD
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 30
    }
}
