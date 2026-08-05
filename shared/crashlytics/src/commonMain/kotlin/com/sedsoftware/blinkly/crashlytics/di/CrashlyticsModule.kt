package com.sedsoftware.blinkly.crashlytics.di

import com.sedsoftware.blinkly.crashlytics.impl.BlinklyCrashReporterImpl
import com.sedsoftware.blinkly.domain.external.BlinklyCrashReporter

interface CrashlyticsModule {
    val crashReporter: BlinklyCrashReporter
}

@Suppress("FunctionName")
fun CrashlyticsModule(): CrashlyticsModule =
    object : CrashlyticsModule {
        override val crashReporter: BlinklyCrashReporter by lazy {
            BlinklyCrashReporterImpl()
        }
    }
