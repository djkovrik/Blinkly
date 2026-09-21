package com.sedsoftware.blinkly.settings

import android.content.Context

fun readBlinklyAnalyticsBootstrapState(
    context: Context,
): BlinklyAnalyticsBootstrapState =
    readBlinklyAnalyticsBootstrapState(
        settings = SharedSettingsFactory(context),
    )
