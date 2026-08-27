package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.external.BlinklyAnalyticsReporter
import com.sedsoftware.blinkly.domain.model.BlinklyAnalyticsEvent
import com.sedsoftware.blinkly.domain.model.toPayload

interface BlinklyAnalytics {
    fun report(event: BlinklyAnalyticsEvent)
    fun setEnabled(enabled: Boolean)
}

object NoOpBlinklyAnalytics : BlinklyAnalytics {
    override fun report(event: BlinklyAnalyticsEvent) = Unit
    override fun setEnabled(enabled: Boolean) = Unit
}

fun createBlinklyAnalytics(
    reporter: BlinklyAnalyticsReporter,
    initiallyEnabled: Boolean,
): BlinklyAnalytics = BlinklyAnalyticsImpl(reporter, initiallyEnabled)

private class BlinklyAnalyticsImpl(
    private val reporter: BlinklyAnalyticsReporter,
    initiallyEnabled: Boolean,
) : BlinklyAnalytics {

    private var enabled: Boolean = initiallyEnabled

    override fun report(event: BlinklyAnalyticsEvent) {
        if (!enabled) return

        val payload = event.toPayload()
        runCatching { reporter.reportEvent(payload.name, payload.parameters) }
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        runCatching { reporter.setDataSendingEnabled(enabled) }
    }
}
