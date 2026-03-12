package com.sedsoftware.blinkly.settings

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.russhwolf.settings.MapSettings
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.settings.impl.BlinklySettingsImpl
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class BlinklySettingsTest {

    private val settings: BlinklySettings = BlinklySettingsImpl(MapSettings())

    @Test
    fun `when get defaults then return proper values`() {
        // given
        // when
        val blinkBreakCountDefault = settings.blinkBreakCount
        val nearFarFocusCountDefault = settings.nearFarFocusCount
        val nearFarFocusDurationDefault = settings.nearFarFocusDuration
        val diagonalGazesCountDefault = settings.diagonalGazesCount
        val diagonalGazesDurationDefault = settings.diagonalGazesDuration
        val figureEightCountDefault = settings.figureEightCount
        val clockRollsEachSideDefault = settings.clockRollsEachSide
        val palmingDurationDefault = settings.palmingDuration
        val themeStateDefault = settings.themeState
        val lightThemeWorkoutDoneDefault = settings.lightThemeWorkoutIndex
        val darkThemeWorkoutDoneDefault = settings.darkThemeWorkoutIndex
        val lastTreeProgressCheckDateDefault = settings.lastTreeProgressCheckDate

        // then
        assertThat(blinkBreakCountDefault).isEqualTo(BLINK_BREAK_COUNT_DEFAULT)
        assertThat(nearFarFocusCountDefault).isEqualTo(NEAR_FOCUS_COUNT_DEFAULT)
        assertThat(nearFarFocusDurationDefault).isEqualTo(NEAR_FOCUS_DURATION_DEFAULT)
        assertThat(diagonalGazesCountDefault).isEqualTo(DIAGONAL_GAZES_COUNT_DEFAULT)
        assertThat(diagonalGazesDurationDefault).isEqualTo(DIAGONAL_GAZES_DURATION_DEFAULT)
        assertThat(figureEightCountDefault).isEqualTo(FIGURE_EIGHT_COUNT_DEFAULT)
        assertThat(clockRollsEachSideDefault).isEqualTo(CLOCK_ROLLS_COUNT_DEFAULT)
        assertThat(palmingDurationDefault).isEqualTo(PALMING_DURATION_DEFAULT)
        assertThat(themeStateDefault).isEqualTo(ThemeState.SYSTEM)
        assertThat(lightThemeWorkoutDoneDefault).isEqualTo(0)
        assertThat(darkThemeWorkoutDoneDefault).isEqualTo(0)
        assertThat(lastTreeProgressCheckDateDefault).isEqualTo(null)
    }

    @Test
    fun `when set and get called then returned proper values`() {
        // given
        val intValue = 12345
        val floatValue = 12345.0f
        val localDateValue = LocalDate.parse("2026-03-01")
        val themeValue = ThemeState.DARK
        // when
        settings.blinkBreakCount = intValue
        settings.nearFarFocusCount = intValue
        settings.nearFarFocusDuration = floatValue
        settings.diagonalGazesCount = intValue
        settings.diagonalGazesDuration = floatValue
        settings.figureEightCount = intValue
        settings.clockRollsEachSide = intValue
        settings.palmingDuration = intValue
        settings.themeState = themeValue
        settings.lightThemeWorkoutIndex = intValue
        settings.darkThemeWorkoutIndex = intValue
        settings.lastTreeProgressCheckDate = localDateValue
        // then
        assertThat(settings.blinkBreakCount).isEqualTo(intValue)
        assertThat(settings.nearFarFocusCount).isEqualTo(intValue)
        assertThat(settings.nearFarFocusDuration).isEqualTo(floatValue)
        assertThat(settings.diagonalGazesCount).isEqualTo(intValue)
        assertThat(settings.diagonalGazesDuration).isEqualTo(floatValue)
        assertThat(settings.figureEightCount).isEqualTo(intValue)
        assertThat(settings.clockRollsEachSide).isEqualTo(intValue)
        assertThat(settings.palmingDuration).isEqualTo(intValue)
        assertThat(settings.themeState).isEqualTo(themeValue)
        assertThat(settings.lightThemeWorkoutIndex).isEqualTo(intValue)
        assertThat(settings.darkThemeWorkoutIndex).isEqualTo(intValue)
        assertThat(settings.lastTreeProgressCheckDate).isEqualTo(localDateValue)
    }

    private companion object {
        const val BLINK_BREAK_COUNT_DEFAULT = 60
        const val NEAR_FOCUS_COUNT_DEFAULT = 10
        const val NEAR_FOCUS_DURATION_DEFAULT = 5f
        const val DIAGONAL_GAZES_COUNT_DEFAULT = 5
        const val DIAGONAL_GAZES_DURATION_DEFAULT = 3f
        const val FIGURE_EIGHT_COUNT_DEFAULT = 10
        const val CLOCK_ROLLS_COUNT_DEFAULT = 5
        const val PALMING_DURATION_DEFAULT = 120
    }
}
