package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.FakeData
import com.sedsoftware.blinkly.domain.achievement.BaseAchievementTest
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

internal class MasterOfClarityTest : BaseAchievementTest() {

    override val achievement: UnlockableAchievement = MasterOfClarity()

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val achievements = FakeData.getFullAchievementsList(now)
        // when
        val unlocked = achievement.unlocked(achievements, emptyCalendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val achievements = FakeData.getFullAchievementsList(now).toMutableList()
        achievements.removeAt(2)
        achievements.removeAt(4)
        achievements.removeAt(6)
        // when
        val unlocked = achievement.unlocked(achievements, emptyCalendar)
        // then
        assertThat(unlocked).isFalse()
    }

    @Test
    fun `regular list should not unlock the achievement`() = runTest {
        // given
        val achievements = FakeData.getRegularAchievementsList(now)
        // when
        val unlocked = achievement.unlocked(achievements, emptyCalendar)
        // then
        assertThat(unlocked).isFalse()
    }
}
