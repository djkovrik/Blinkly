package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #40
 * Think Tank - Do 10 different exercises in a row without repeating any
 */
internal class ThinkTank : UnlockableAchievement {

    override val type: AchievementType = AchievementType.THINK_TANK

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val allExercises = calendar.flatMap { it.exercises }

        if (allExercises.size < ACHIEVEMENT_THRESHOLD) return false

        var consecutiveNonRepeating = 1

        for (i in 1 until allExercises.size) {
            if (allExercises[i].type != allExercises[i - 1].type) {
                consecutiveNonRepeating++
                if (consecutiveNonRepeating >= ACHIEVEMENT_THRESHOLD) {
                    return true
                }
            } else {
                consecutiveNonRepeating = 1
            }
        }

        return false
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 10
    }
}
