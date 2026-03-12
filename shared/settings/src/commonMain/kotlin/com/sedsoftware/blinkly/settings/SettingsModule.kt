package com.sedsoftware.blinkly.settings

import com.russhwolf.settings.Settings
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.settings.internal.BlinklySettingsImpl

interface SettingsModule {
    val settings: BlinklySettings
}

interface SettingsModuleDependencies {
    val settings: Settings
}

fun SettingsModule(dependencies: SettingsModuleDependencies): SettingsModule {
    return object : SettingsModule {
        override val settings: BlinklySettings by lazy {
            BlinklySettingsImpl(dependencies.settings)
        }
    }
}
