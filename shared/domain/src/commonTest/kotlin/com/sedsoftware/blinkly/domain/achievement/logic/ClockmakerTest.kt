package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.base.BaseAchievementTest
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

internal class ClockmakerTest : BaseAchievementTest() {

    private val clockRollsEachSide: Int = 5

    override val achievement: UnlockableAchievement = Clockmaker(clockRollsEachSide)

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val calendar = clockRollsCalendar(10)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val calendar = clockRollsCalendar(9)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isFalse()
    }

    private fun clockRollsCalendar(count: Int): List<Workout> =
        listOf(
            Workout(
                exercises = List(count) {
                    Exercise(ExerciseBlock.B, ExerciseType.CLOCK_ROLLS, now)
                }
            )
        )
}
