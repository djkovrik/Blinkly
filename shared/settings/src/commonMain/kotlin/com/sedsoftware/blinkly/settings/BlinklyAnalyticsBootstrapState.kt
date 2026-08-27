package com.sedsoftware.blinkly.settings

import com.russhwolf.settings.Settings

data class BlinklyAnalyticsBootstrapState(
    val analyticsEnabled: Boolean,
    val existingInstallation: Boolean,
)

fun readBlinklyAnalyticsBootstrapState(
    settings: Settings,
    databaseExists: Boolean = false,
): BlinklyAnalyticsBootstrapState =
    BlinklyAnalyticsBootstrapState(
        analyticsEnabled = settings.getBoolean(ANALYTICS_ENABLED_KEY, true),
        existingInstallation = databaseExists || EXISTING_INSTALLATION_KEYS.any(settings::hasKey),
    )

private const val ANALYTICS_ENABLED_KEY = "ae"

private val EXISTING_INSTALLATION_KEYS = listOf(
    "od",
    "bc",
    "nfc",
    "ts",
    "ltwi",
    "dtwi",
)
