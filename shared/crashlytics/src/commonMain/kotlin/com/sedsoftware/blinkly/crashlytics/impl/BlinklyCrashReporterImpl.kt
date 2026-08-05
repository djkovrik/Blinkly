package com.sedsoftware.blinkly.crashlytics.impl

import com.sedsoftware.blinkly.domain.external.BlinklyCrashReporter
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

internal class BlinklyCrashReporterImpl : BlinklyCrashReporter {

    private val crashlytics by lazy { Firebase.crashlytics }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
}
