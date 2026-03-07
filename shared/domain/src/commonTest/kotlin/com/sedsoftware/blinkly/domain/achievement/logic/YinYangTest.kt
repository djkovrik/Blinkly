package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.achievement.BaseAchievementTest
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class YinYangTest : BaseAchievementTest() {
    private var _lightThemeDone: Boolean = false
    private var _darkThemeDone: Boolean = false
    private val lightThemeDone = { _lightThemeDone }
    private val darkThemeDone = { _darkThemeDone }

    override val achievement: UnlockableAchievement = YinYang(lightThemeDone, darkThemeDone)

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        _lightThemeDone = true
        _darkThemeDone = true
        // when
        val unlocked = achievement.unlocked(emptyAchievements, emptyCalendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        _lightThemeDone = false
        _darkThemeDone = true
        // when
        var unlocked = achievement.unlocked(emptyAchievements, emptyCalendar)
        // then
        assertThat(unlocked).isFalse()

        // given
        _lightThemeDone = true
        _darkThemeDone = false
        // when
        unlocked = achievement.unlocked(emptyAchievements, emptyCalendar)
        // then
        assertThat(unlocked).isFalse()
    }
}
