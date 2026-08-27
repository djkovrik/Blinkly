package com.sedsoftware.blinkly.settings

import android.content.Context

fun readBlinklyAnalyticsBootstrapState(
    context: Context,
    databaseExists: Boolean,
): BlinklyAnalyticsBootstrapState =
    readBlinklyAnalyticsBootstrapState(
        settings = SharedSettingsFactory(context),
        databaseExists = databaseExists,
    )
