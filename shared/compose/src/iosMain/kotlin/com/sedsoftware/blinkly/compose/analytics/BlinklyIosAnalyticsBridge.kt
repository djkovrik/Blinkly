package com.sedsoftware.blinkly.compose.analytics

import com.sedsoftware.blinkly.domain.external.BlinklyAnalyticsReporter

/**
 * Swift-facing analytics boundary owned by the exported Compose framework.
 *
 * Keep types from implementation modules out of this API: dependencies of the
 * framework are not exported to Swift unless they are explicitly re-exported.
 */
interface BlinklyIosAnalyticsReporter {
    fun reportEvent(name: String, parameters: Map<String, String>)

    fun setDataSendingEnabled(enabled: Boolean)
}

class BlinklyIosAnalyticsBootstrapState(
    val analyticsEnabled: Boolean,
    val existingInstallation: Boolean,
)

internal fun BlinklyIosAnalyticsReporter.asDomainReporter(): BlinklyAnalyticsReporter =
    object : BlinklyAnalyticsReporter {
        override fun reportEvent(name: String, parameters: Map<String, String>) {
            this@asDomainReporter.reportEvent(name, parameters)
        }

        override fun setDataSendingEnabled(enabled: Boolean) {
            this@asDomainReporter.setDataSendingEnabled(enabled)
        }
    }
