package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.FakeData
import com.sedsoftware.blinkly.domain.achievement.BaseAchievementTest
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

internal class ThirtyExercisesTest : BaseAchievementTest() {

    override val achievement: UnlockableAchievement = ThirtyExercises()

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val calendar = FakeData.getCalendarWithFullAndPartialDays(now, 20)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val calendar = FakeData.getCalendarWithFullAndPartialDays(now, 3)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isFalse()
    }
}
