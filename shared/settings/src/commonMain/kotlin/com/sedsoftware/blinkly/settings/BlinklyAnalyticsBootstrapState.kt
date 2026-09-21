package com.sedsoftware.blinkly.settings

import com.russhwolf.settings.Settings

data class BlinklyAnalyticsBootstrapState(
    val analyticsEnabled: Boolean,
)

fun readBlinklyAnalyticsBootstrapState(
    settings: Settings,
): BlinklyAnalyticsBootstrapState =
    BlinklyAnalyticsBootstrapState(
        analyticsEnabled = settings.getBoolean(ANALYTICS_ENABLED_KEY, true),
    )

private const val ANALYTICS_ENABLED_KEY = "ae"
