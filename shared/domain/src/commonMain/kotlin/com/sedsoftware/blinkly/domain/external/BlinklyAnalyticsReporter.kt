package com.sedsoftware.blinkly.domain.external

interface BlinklyAnalyticsReporter {
    fun reportEvent(name: String, parameters: Map<String, String>)
    fun setDataSendingEnabled(enabled: Boolean)
}

object NoOpBlinklyAnalyticsReporter : BlinklyAnalyticsReporter {
    override fun reportEvent(name: String, parameters: Map<String, String>) = Unit

    override fun setDataSendingEnabled(enabled: Boolean) = Unit
}
