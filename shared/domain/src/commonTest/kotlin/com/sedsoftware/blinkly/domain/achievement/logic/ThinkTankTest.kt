package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.fakes.FakeData
import com.sedsoftware.blinkly.domain.base.BaseAchievementTest
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

internal class ThinkTankTest : BaseAchievementTest() {

    override val achievement: UnlockableAchievement = ThinkTank()

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val calendar = FakeData.getThinkTankWorkoutCorrect(now)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val calendar = FakeData.getThinkTankWorkoutIncorrect(now)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isFalse()
    }

    @Test
    fun `when ten exercises repeat consecutively then not unlocked`() = runTest {
        // given
        val calendar = listOf(
            Workout(
                exercises = List(10) {
                    Exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, now)
                }
            )
        )

        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)

        // then
        assertThat(unlocked).isFalse()
    }
}
