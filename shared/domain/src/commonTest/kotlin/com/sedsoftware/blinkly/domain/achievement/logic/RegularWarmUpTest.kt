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

internal class RegularWarmUpTest : BaseAchievementTest() {

    override val achievement: UnlockableAchievement = RegularWarmUp()

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val calendar = FakeData.getCalendarWithFullBlocksA(now, 10)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val calendar = FakeData.getCalendarWithFullBlocksA(now, 9)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isFalse()
    }

    @Test
    fun `when block A is completed ten times on the same day then unlocked`() = runTest {
        // given
        val exercises = List(10) {
            listOf(
                Exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, now),
                Exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS, now),
                Exercise(ExerciseBlock.A, ExerciseType.DIAGONAL_GAZES, now),
            )
        }.flatten()
        val calendar = listOf(Workout(exercises))

        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)

        // then
        assertThat(unlocked).isTrue()
    }
}
