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

internal class EveningRitualTest : BaseAchievementTest() {

    override val achievement: UnlockableAchievement = EveningRitual()

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val calendar = FakeData.getCalendarWithFullBlocksB(now, 10)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val calendar = FakeData.getCalendarWithFullBlocksB(now, 9)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isFalse()
    }

    @Test
    fun `when block B is completed ten times on the same day then unlocked`() = runTest {
        // given
        val exercises = List(10) {
            listOf(
                Exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT, now),
                Exercise(ExerciseBlock.B, ExerciseType.CLOCK_ROLLS, now),
                Exercise(ExerciseBlock.B, ExerciseType.PALMING, now),
            )
        }.flatten()
        val calendar = listOf(Workout(exercises))

        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)

        // then
        assertThat(unlocked).isTrue()
    }
}
