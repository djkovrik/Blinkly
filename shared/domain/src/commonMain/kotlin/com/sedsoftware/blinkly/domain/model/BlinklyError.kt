package com.sedsoftware.blinkly.domain.model

sealed class BlinklyError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class MainDataLoading(cause: Throwable) : BlinklyError("Unable to load main screen data", cause)
    class ProgressDataLoading(cause: Throwable) : BlinklyError("Unable to load progress data", cause)
    class TrainingsDataLoading(cause: Throwable) : BlinklyError("Unable to load trainings data", cause)
    class PreferencesLoading(cause: Throwable) : BlinklyError("Unable to load preferences", cause)
    class PreferencesSaving(cause: Throwable) : BlinklyError("Unable to save preferences", cause)
    class AchievementsLoading(cause: Throwable) : BlinklyError("Unable to load achievements", cause)
    class GardenLoading(cause: Throwable) : BlinklyError("Unable to load garden", cause)
    class RemindersLoading(cause: Throwable) : BlinklyError("Unable to load reminders", cause)
    class ReminderDeleting(cause: Throwable) : BlinklyError("Unable to delete reminder", cause)
    class ReminderRestoring(cause: Throwable) : BlinklyError("Unable to restore reminder", cause)
    class ReminderCreating(cause: Throwable) : BlinklyError("Unable to create reminder", cause)
    class InitialRemindersLoading(cause: Throwable) : BlinklyError("Unable to load initial reminders", cause)
    class InitialRemindersCreating(cause: Throwable) : BlinklyError("Unable to create initial reminders", cause)
    class InitialRemindersClearing(cause: Throwable) : BlinklyError("Unable to clear initial reminders", cause)
    class NotificationPermissionChecking(cause: Throwable) : BlinklyError("Unable to check notification permission", cause)
    class NotificationPermissionRequesting(cause: Throwable) : BlinklyError("Unable to request notification permission", cause)
    class WorkoutDataLoading(cause: Throwable) : BlinklyError("Unable to update workout data", cause)
    class WorkoutSaving(cause: Throwable) : BlinklyError("Unable to save workout", cause)
    class Unknown(cause: Throwable) : BlinklyError("Unknown error", cause)
}

fun Throwable.asBlinklyError(mapper: (Throwable) -> BlinklyError): BlinklyError =
    when (this) {
        is BlinklyError -> this
        else -> mapper(this)
    }
