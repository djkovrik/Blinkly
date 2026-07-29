package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.fakes.FakeData
import com.sedsoftware.blinkly.domain.base.BaseAchievementTest
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

internal class EncyclopediaOfSightTest : BaseAchievementTest() {

    override val achievement: UnlockableAchievement = EncyclopediaOfSight()

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val achievements = achievementsBeforeEncyclopedia()
        // when
        val unlocked = achievement.unlocked(achievements, emptyCalendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val achievements = achievementsBeforeEncyclopedia()
            .filter { it.type != AchievementType.CLOCKWISE }
        // when
        val unlocked = achievement.unlocked(achievements, emptyCalendar)
        // then
        assertThat(unlocked).isFalse()
    }

    @Test
    fun `when a required achievement exists but is locked then not unlocked`() = runTest {
        // given
        val achievements = achievementsBeforeEncyclopedia()
            .map { item ->
                if (item.type == AchievementType.CLOCKWISE) item.copy(unlockedAt = null) else item
            }

        // when
        val unlocked = achievement.unlocked(achievements, emptyCalendar)

        // then
        assertThat(unlocked).isFalse()
    }

    private fun achievementsBeforeEncyclopedia() =
        FakeData.getFullAchievementsList(now)
            .filter { it.type.index < AchievementType.ENCYCLOPEDIA_OF_SIGHT.index }
}
