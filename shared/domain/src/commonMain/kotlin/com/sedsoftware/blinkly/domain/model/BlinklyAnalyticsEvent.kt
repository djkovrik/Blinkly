package com.sedsoftware.blinkly.domain.model

sealed interface BlinklyAnalyticsEvent {
    data object OnboardingStarted : BlinklyAnalyticsEvent
    data object OnboardingCompleted : BlinklyAnalyticsEvent
    data class TabOpened(val tab: Tab) : BlinklyAnalyticsEvent
    data class TrainingStarted(val source: TrainingSource) : BlinklyAnalyticsEvent
    data class TrainingFinished(val result: TrainingResult) : BlinklyAnalyticsEvent
    data class ReminderFlowOpened(val source: ReminderSource) : BlinklyAnalyticsEvent
    data class ReminderCreated(val kind: ReminderKind) : BlinklyAnalyticsEvent
    data class ProgressDetailsOpened(val screen: ProgressScreen) : BlinklyAnalyticsEvent
    data object PreferencesOpened : BlinklyAnalyticsEvent
    data class SyncActionFinished(val result: SyncResult) : BlinklyAnalyticsEvent

    enum class Tab { MAIN, TRAININGS, PROGRESS, REMINDERS }
    enum class TrainingResult { COMPLETED, CANCELLED }
    enum class ReminderSource { ONBOARDING, REMINDERS }
    enum class ReminderKind { DAILY, WEEKLY, WORKDAY_PERIOD }
    enum class ProgressScreen { ACHIEVEMENTS, GARDEN }
    enum class SyncResult { SUCCESS, CANCELLED, FAILURE }
}

internal data class BlinklyAnalyticsPayload(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
)

internal fun BlinklyAnalyticsEvent.toPayload(): BlinklyAnalyticsPayload =
    when (this) {
        BlinklyAnalyticsEvent.OnboardingStarted -> BlinklyAnalyticsPayload("onboarding_started")
        BlinklyAnalyticsEvent.OnboardingCompleted -> BlinklyAnalyticsPayload("onboarding_completed")
        is BlinklyAnalyticsEvent.TabOpened -> BlinklyAnalyticsPayload("tab_opened", mapOf("tab" to tab.value))
        is BlinklyAnalyticsEvent.TrainingStarted ->
            BlinklyAnalyticsPayload("training_started", mapOf("source" to source.value))
        is BlinklyAnalyticsEvent.TrainingFinished ->
            BlinklyAnalyticsPayload("training_finished", mapOf("result" to result.value))
        is BlinklyAnalyticsEvent.ReminderFlowOpened ->
            BlinklyAnalyticsPayload("reminder_flow_opened", mapOf("source" to source.value))
        is BlinklyAnalyticsEvent.ReminderCreated ->
            BlinklyAnalyticsPayload("reminder_created", mapOf("kind" to kind.value))
        is BlinklyAnalyticsEvent.ProgressDetailsOpened ->
            BlinklyAnalyticsPayload("progress_details_opened", mapOf("screen" to screen.value))
        BlinklyAnalyticsEvent.PreferencesOpened -> BlinklyAnalyticsPayload("preferences_opened")
        is BlinklyAnalyticsEvent.SyncActionFinished ->
            BlinklyAnalyticsPayload("sync_action_finished", mapOf("result" to result.value))
    }

private val BlinklyAnalyticsEvent.Tab.value: String get() = name.lowercase()
private val TrainingSource.value: String get() = name.lowercase()
private val BlinklyAnalyticsEvent.TrainingResult.value: String get() = name.lowercase()
private val BlinklyAnalyticsEvent.ReminderSource.value: String get() = name.lowercase()
private val BlinklyAnalyticsEvent.ReminderKind.value: String get() = name.lowercase()
private val BlinklyAnalyticsEvent.ProgressScreen.value: String get() = name.lowercase()
private val BlinklyAnalyticsEvent.SyncResult.value: String get() = name.lowercase()
