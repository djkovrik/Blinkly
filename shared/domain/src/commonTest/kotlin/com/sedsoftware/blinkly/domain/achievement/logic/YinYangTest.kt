package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.base.BaseAchievementTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class YinYangTest : BaseAchievementTest() {
    private var _lightThemeIndex: Int = 0
    private var _darkThemeIndex: Int = 0
    private val lightThemeDone = { _lightThemeIndex }
    private val darkThemeDone = { _darkThemeIndex }

    override val achievement: UnlockableAchievement = YinYang(lightThemeDone, darkThemeDone)

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        _lightThemeIndex = 1
        _darkThemeIndex = 2
        // when
        val unlocked = achievement.unlocked(emptyAchievements, emptyCalendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        _lightThemeIndex = 0
        _darkThemeIndex = 1
        // when
        var unlocked = achievement.unlocked(emptyAchievements, emptyCalendar)
        // then
        assertThat(unlocked).isFalse()

        // given
        _lightThemeIndex = 1
        _darkThemeIndex = 0
        // when
        unlocked = achievement.unlocked(emptyAchievements, emptyCalendar)
        // then
        assertThat(unlocked).isFalse()
    }
}
