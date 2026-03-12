package com.sedsoftware.blinkly.domain.internal

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class YinYangWatcherTest {

    private val watcher: YinYangWatcher = YinYangWatcherImpl()

    @Test
    fun `when ThemeState - LIGHT theme then save related option`() {
        // given
        val settings = getSettings()
        settings.themeState = ThemeState.LIGHT
        // when
        assertThat(settings.lightThemeWorkoutDone).isFalse()
        watcher.onBlockCompleted(settings)
        // then
        assertThat(settings.lightThemeWorkoutDone).isTrue()
    }

    @Test
    fun `when ThemeState - DARK theme then save related option`() {
        // given
        val settings = getSettings()
        settings.themeState = ThemeState.DARK
        // when
        assertThat(settings.darkThemeWorkoutDone).isFalse()
        watcher.onBlockCompleted(settings)
        // then
        assertThat(settings.darkThemeWorkoutDone).isTrue()
    }

    @Test
    fun `when ThemeState - SYSTEM theme then no options saved`() {
        // given
        val settings = getSettings()
        settings.themeState = ThemeState.SYSTEM
        // when
        assertThat(settings.lightThemeWorkoutDone).isFalse()
        assertThat(settings.darkThemeWorkoutDone).isFalse()
        watcher.onBlockCompleted(settings)
        // then
        assertThat(settings.lightThemeWorkoutDone).isFalse()
        assertThat(settings.darkThemeWorkoutDone).isFalse()
    }

    private fun getSettings(): BlinklySettings =
        object : BlinklySettings {
            override var blinkBreakCount: Int = 0
            override var nearFarFocusCount: Int = 0
            override var nearFarFocusDuration: Float = 0f
            override var diagonalGazesCount: Int = 0
            override var diagonalGazesDuration: Float = 0f
            override var figureEightCount: Int = 0
            override var clockRollsEachSide: Int = 0
            override var palmingDuration: Int = 0
            override var themeState: ThemeState = ThemeState.SYSTEM
            override var lightThemeWorkoutDone: Boolean = false
            override var darkThemeWorkoutDone: Boolean = false
            override var lastTreeProgressCheckDate: LocalDate? = null
        }
}
