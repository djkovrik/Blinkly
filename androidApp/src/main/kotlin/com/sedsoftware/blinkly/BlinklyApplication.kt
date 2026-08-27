package com.sedsoftware.blinkly

import android.app.Application
import com.sedsoftware.blinkly.domain.external.BlinklyAnalyticsReporter
import com.sedsoftware.blinkly.domain.external.NoOpBlinklyAnalyticsReporter
import com.sedsoftware.blinkly.settings.readBlinklyAnalyticsBootstrapState
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

class BlinklyApplication : Application() {

    lateinit var analyticsReporter: BlinklyAnalyticsReporter
        private set

    override fun onCreate() {
        super.onCreate()

        analyticsReporter = if (BuildConfig.DEBUG) {
            NoOpBlinklyAnalyticsReporter
        } else {
            val bootstrapState = readBlinklyAnalyticsBootstrapState(
                context = this,
                databaseExists = getDatabasePath(DATABASE_NAME).exists(),
            )
            val config = AppMetricaConfig.newConfigBuilder(BuildConfig.BLINKLY_APPMETRICA_API_KEY)
                .withAdvIdentifiersTracking(false)
                .withLocationTracking(false)
                .withCrashReporting(false)
                .withNativeCrashReporting(false)
                .withRevenueAutoTrackingEnabled(false)
                .withDataSendingEnabled(bootstrapState.analyticsEnabled)
                .handleFirstActivationAsUpdate(bootstrapState.existingInstallation)
                .build()

            AppMetrica.activate(this, config)
            AppMetrica.enableActivityAutoTracking(this)
            AppMetricaBlinklyAnalyticsReporter
        }
    }

    private companion object {
        const val DATABASE_NAME = "blinkly_database.db"
    }
}

private object AppMetricaBlinklyAnalyticsReporter : BlinklyAnalyticsReporter {
    override fun reportEvent(name: String, parameters: Map<String, String>) {
        AppMetrica.reportEvent(name, parameters)
    }

    override fun setDataSendingEnabled(enabled: Boolean) {
        AppMetrica.setDataSendingEnabled(enabled)
    }
}
