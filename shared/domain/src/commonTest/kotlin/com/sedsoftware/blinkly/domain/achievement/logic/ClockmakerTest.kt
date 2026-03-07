package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.FakeData
import com.sedsoftware.blinkly.domain.achievement.BaseAchievementTest
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.ExerciseType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

internal class ClockmakerTest : BaseAchievementTest() {

    override val achievement: UnlockableAchievement = Clockmaker()

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val calendar = FakeData.getCalendarWithMinExerciseCount(now, ExerciseType.CLOCK_ROLLS, 50)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val calendar = FakeData.getCalendarWithMinExerciseCount(now, ExerciseType.CLOCK_ROLLS, 25)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isFalse()
    }
}
