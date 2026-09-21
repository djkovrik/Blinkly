package com.sedsoftware.blinkly

import android.app.Application
import android.os.Bundle
import com.sedsoftware.blinkly.domain.external.BlinklyAnalyticsReporter
import com.sedsoftware.blinkly.domain.external.NoOpBlinklyAnalyticsReporter
import com.sedsoftware.blinkly.settings.readBlinklyAnalyticsBootstrapState
import com.google.firebase.analytics.FirebaseAnalytics

class BlinklyApplication : Application() {

    lateinit var analyticsReporter: BlinklyAnalyticsReporter
        private set

    override fun onCreate() {
        super.onCreate()

        analyticsReporter = if (BuildConfig.DEBUG) {
            NoOpBlinklyAnalyticsReporter
        } else {
            val analytics = FirebaseAnalytics.getInstance(this)
            val bootstrapState = readBlinklyAnalyticsBootstrapState(context = this)
            analytics.setAnalyticsCollectionEnabled(bootstrapState.analyticsEnabled)
            FirebaseBlinklyAnalyticsReporter(analytics)
        }
    }
}

private class FirebaseBlinklyAnalyticsReporter(
    private val analytics: FirebaseAnalytics,
) : BlinklyAnalyticsReporter {

    override fun reportEvent(name: String, parameters: Map<String, String>) {
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) -> putString(key, value) }
        }
        analytics.logEvent(name, bundle)
    }

    override fun setDataSendingEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }
}
