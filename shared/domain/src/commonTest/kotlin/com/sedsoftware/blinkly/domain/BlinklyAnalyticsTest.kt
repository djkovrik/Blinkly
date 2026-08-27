package com.sedsoftware.blinkly.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.sedsoftware.blinkly.domain.external.BlinklyAnalyticsReporter
import com.sedsoftware.blinkly.domain.model.BlinklyAnalyticsEvent
import com.sedsoftware.blinkly.domain.model.TrainingSource
import kotlin.test.Test

class BlinklyAnalyticsTest {

    @Test
    fun `events map to the approved schema`() {
        val reporter = RecordingReporter()
        val analytics = createBlinklyAnalytics(reporter, initiallyEnabled = true)

        approvedEvents().forEach(analytics::report)

        assertThat(reporter.events).containsExactly(
            RecordedEvent("onboarding_started"),
            RecordedEvent("onboarding_completed"),
            RecordedEvent("tab_opened", mapOf("tab" to "main")),
            RecordedEvent("training_started", mapOf("source" to "main")),
            RecordedEvent("training_finished", mapOf("result" to "completed")),
            RecordedEvent("reminder_flow_opened", mapOf("source" to "reminders")),
            RecordedEvent("reminder_created", mapOf("kind" to "workday_period")),
            RecordedEvent("progress_details_opened", mapOf("screen" to "achievements")),
            RecordedEvent("preferences_opened"),
            RecordedEvent("sync_action_finished", mapOf("result" to "cancelled")),
        )
    }

    @Test
    fun `disabled analytics suppresses events and controls reporter`() {
        val reporter = RecordingReporter()
        val analytics = createBlinklyAnalytics(reporter, initiallyEnabled = true)

        analytics.setEnabled(false)
        analytics.report(BlinklyAnalyticsEvent.PreferencesOpened)
        analytics.setEnabled(true)
        analytics.report(BlinklyAnalyticsEvent.PreferencesOpened)

        assertThat(reporter.enabledChanges).containsExactly(false, true)
        assertThat(reporter.events).containsExactly(RecordedEvent("preferences_opened"))
    }

    @Test
    fun `initially disabled analytics does not report`() {
        val reporter = RecordingReporter()
        val analytics = createBlinklyAnalytics(reporter, initiallyEnabled = false)

        analytics.report(BlinklyAnalyticsEvent.OnboardingStarted)

        assertThat(reporter.events).isEmpty()
    }

    private fun approvedEvents(): List<BlinklyAnalyticsEvent> =
        listOf(
            BlinklyAnalyticsEvent.OnboardingStarted,
            BlinklyAnalyticsEvent.OnboardingCompleted,
            BlinklyAnalyticsEvent.TabOpened(BlinklyAnalyticsEvent.Tab.MAIN),
            BlinklyAnalyticsEvent.TrainingStarted(TrainingSource.MAIN),
            BlinklyAnalyticsEvent.TrainingFinished(BlinklyAnalyticsEvent.TrainingResult.COMPLETED),
            BlinklyAnalyticsEvent.ReminderFlowOpened(BlinklyAnalyticsEvent.ReminderSource.REMINDERS),
            BlinklyAnalyticsEvent.ReminderCreated(BlinklyAnalyticsEvent.ReminderKind.WORKDAY_PERIOD),
            BlinklyAnalyticsEvent.ProgressDetailsOpened(BlinklyAnalyticsEvent.ProgressScreen.ACHIEVEMENTS),
            BlinklyAnalyticsEvent.PreferencesOpened,
            BlinklyAnalyticsEvent.SyncActionFinished(BlinklyAnalyticsEvent.SyncResult.CANCELLED),
        )

    private class RecordingReporter : BlinklyAnalyticsReporter {
        val events = mutableListOf<RecordedEvent>()
        val enabledChanges = mutableListOf<Boolean>()

        override fun reportEvent(name: String, parameters: Map<String, String>) {
            events += RecordedEvent(name, parameters)
        }

        override fun setDataSendingEnabled(enabled: Boolean) {
            enabledChanges += enabled
        }
    }

    private data class RecordedEvent(
        val name: String,
        val parameters: Map<String, String> = emptyMap(),
    )
}
