package com.sedsoftware.blinkly.settings.impl

import com.russhwolf.settings.Settings
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.LocalDate

internal class BlinklySettingsImpl(
    private val settings: Settings,
) : BlinklySettings {

    override var blinkBreakCount: Int
        get() = settings.getValue(PREF_BLINK_BREAK_COUNT, BLINK_BREAK_COUNT_DEFAULT)
        set(value) {
            settings.setValue(PREF_BLINK_BREAK_COUNT, value)
        }

    override var nearFarFocusCount: Int
        get() = settings.getValue(PREF_NEAR_FOCUS_COUNT, NEAR_FOCUS_COUNT_DEFAULT)
        set(value) {
            settings.setValue(PREF_NEAR_FOCUS_COUNT, value)
        }

    override var nearFarFocusDuration: Float
        get() = settings.getValue(PREF_NEAR_FOCUS_DURATION, NEAR_FOCUS_DURATION_DEFAULT)
        set(value) {
            settings.setValue(PREF_NEAR_FOCUS_DURATION, value)
        }

    override var diagonalGazesCount: Int
        get() = settings.getValue(PREF_DIAGONAL_GAZES_COUNT, DIAGONAL_GAZES_COUNT_DEFAULT)
        set(value) {
            settings.setValue(PREF_DIAGONAL_GAZES_COUNT, value)
        }

    override var diagonalGazesDuration: Float
        get() = settings.getValue(PREF_DIAGONAL_GAZES_DURATION, DIAGONAL_GAZES_DURATION_DEFAULT)
        set(value) {
            settings.setValue(PREF_DIAGONAL_GAZES_DURATION, value)
        }

    override var figureEightCount: Int
        get() = settings.getValue(PREF_FIGURE_EIGHT_COUNT, FIGURE_EIGHT_COUNT_DEFAULT)
        set(value) {
            settings.setValue(PREF_FIGURE_EIGHT_COUNT, value)
        }

    override var clockRollsEachSide: Int
        get() = settings.getValue(PREF_CLOCK_ROLLS_COUNT, CLOCK_ROLLS_COUNT_DEFAULT)
        set(value) {
            settings.setValue(PREF_CLOCK_ROLLS_COUNT, value)
        }

    override var palmingDuration: Int
        get() = settings.getValue(PREF_PALMING_DURATION, PALMING_DURATION_DEFAULT)
        set(value) {
            settings.setValue(PREF_PALMING_DURATION, value)
        }

    override var themeState: ThemeState
        get() = ThemeState.fromInt(settings.getValue(PREF_THEME_STATE, THEME_STATE_DEFAULT))
        set(value) {
            settings.setValue(PREF_THEME_STATE, value.index)
        }

    override var lightThemeWorkoutIndex: Int
        get() = settings.getValue(PREF_LIGHT_THEME_WORKOUT_INDEX, 0)
        set(value) {
            settings.setValue(PREF_LIGHT_THEME_WORKOUT_INDEX, value)
        }

    override var darkThemeWorkoutIndex: Int
        get() = settings.getValue(PREF_DARK_THEME_WORKOUT_INDEX, 0)
        set(value) {
            settings.setValue(PREF_DARK_THEME_WORKOUT_INDEX, value)
        }

    override var lastTreeProgressCheckDate: LocalDate?
        get() {
            val stringValue = settings.getValue(PREF_LAST_TREE_PROGRESS_CHECK_DATE, "")
            return if (stringValue.isNotEmpty()) {
                LocalDate.parse(stringValue)
            } else {
                null
            }
        }
        set(value) {
            val stringValue = value.toString()
            settings.setValue(PREF_LAST_TREE_PROGRESS_CHECK_DATE, stringValue)
        }

    private fun Settings.setValue(key: String, value: Any) {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Long -> putLong(key, value)
            else -> throw UnsupportedOperationException("Not implemented yet")
        }
    }

    private inline fun <reified T : Any> Settings.getValue(key: String, defaultValue: T? = null): T =
        when (T::class) {
            String::class -> getString(key, defaultValue as? String ?: "") as T
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class -> getLong(key, defaultValue as? Long ?: -1L) as T
            else -> throw UnsupportedOperationException("Not implemented yet")
        }

    private companion object {
        const val PREF_BLINK_BREAK_COUNT = "bc"
        const val BLINK_BREAK_COUNT_DEFAULT = 60
        const val PREF_NEAR_FOCUS_COUNT = "nfc"
        const val NEAR_FOCUS_COUNT_DEFAULT = 10
        const val PREF_NEAR_FOCUS_DURATION = "nfd"
        const val NEAR_FOCUS_DURATION_DEFAULT = 5f
        const val PREF_DIAGONAL_GAZES_COUNT = "dgc"
        const val DIAGONAL_GAZES_COUNT_DEFAULT = 5
        const val PREF_DIAGONAL_GAZES_DURATION = "dgd"
        const val DIAGONAL_GAZES_DURATION_DEFAULT = 3f
        const val PREF_FIGURE_EIGHT_COUNT = "fec"
        const val FIGURE_EIGHT_COUNT_DEFAULT = 10
        const val PREF_CLOCK_ROLLS_COUNT = "crc"
        const val CLOCK_ROLLS_COUNT_DEFAULT = 5
        const val PREF_PALMING_DURATION = "pd"
        const val PALMING_DURATION_DEFAULT = 120
        const val PREF_THEME_STATE = "ts"
        const val THEME_STATE_DEFAULT = 0
        const val PREF_LIGHT_THEME_WORKOUT_INDEX = "ltwi"
        const val PREF_DARK_THEME_WORKOUT_INDEX = "dtwi"
        const val PREF_LAST_TREE_PROGRESS_CHECK_DATE = "ltpcd"
    }
}
