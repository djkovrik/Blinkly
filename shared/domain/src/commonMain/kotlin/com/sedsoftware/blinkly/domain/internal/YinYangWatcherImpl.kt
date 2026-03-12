package com.sedsoftware.blinkly.domain.internal

import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ThemeState

internal interface YinYangWatcher {
    fun onBlockCompleted(settings: BlinklySettings)
}

internal class YinYangWatcherImpl : YinYangWatcher {
    override fun onBlockCompleted(settings: BlinklySettings) {
        if (!settings.lightThemeWorkoutDone && settings.themeState == ThemeState.LIGHT) {
            settings.lightThemeWorkoutDone = true
        }

        if (!settings.darkThemeWorkoutDone && settings.themeState == ThemeState.DARK) {
            settings.darkThemeWorkoutDone = true
        }
    }
}
