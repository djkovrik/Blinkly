package com.sedsoftware.blinkly.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.error_achievements_loading
import blinkly.shared.compose.generated.resources.error_exact_alarm_permission_checking
import blinkly.shared.compose.generated.resources.error_exact_alarm_permission_denied
import blinkly.shared.compose.generated.resources.error_exact_alarm_permission_requesting
import blinkly.shared.compose.generated.resources.error_garden_loading
import blinkly.shared.compose.generated.resources.error_initial_reminders_clearing
import blinkly.shared.compose.generated.resources.error_initial_reminders_creating
import blinkly.shared.compose.generated.resources.error_initial_reminders_loading
import blinkly.shared.compose.generated.resources.error_main_data_loading
import blinkly.shared.compose.generated.resources.error_notification_permission_checking
import blinkly.shared.compose.generated.resources.error_notification_permission_denied_always
import blinkly.shared.compose.generated.resources.error_notification_permission_requesting
import blinkly.shared.compose.generated.resources.error_preferences_loading
import blinkly.shared.compose.generated.resources.error_preferences_saving
import blinkly.shared.compose.generated.resources.error_progress_data_loading
import blinkly.shared.compose.generated.resources.error_reminder_creating
import blinkly.shared.compose.generated.resources.error_reminder_deleting
import blinkly.shared.compose.generated.resources.error_reminder_restoring
import blinkly.shared.compose.generated.resources.error_reminders_loading
import blinkly.shared.compose.generated.resources.error_reminders_rescheduling
import blinkly.shared.compose.generated.resources.error_sync_auth_failed
import blinkly.shared.compose.generated.resources.error_sync_conflict_failed
import blinkly.shared.compose.generated.resources.error_sync_read_failed
import blinkly.shared.compose.generated.resources.error_sync_unknown
import blinkly.shared.compose.generated.resources.error_sync_write_failed
import blinkly.shared.compose.generated.resources.error_trainings_data_loading
import blinkly.shared.compose.generated.resources.error_unknown
import blinkly.shared.compose.generated.resources.error_workout_data_loading
import blinkly.shared.compose.generated.resources.error_workout_saving
import blinkly.shared.compose.generated.resources.notification_achievement_unlocked
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.root.RootComponent
import com.sedsoftware.blinkly.compose.ads.BlinklyAdEvent
import com.sedsoftware.blinkly.compose.ads.BlinklyAdsConfiguration
import com.sedsoftware.blinkly.compose.theme.BlinklyAppTheme
import com.sedsoftware.blinkly.compose.ui.ads.BlinklyAdsHost
import com.sedsoftware.blinkly.compose.ui.achievements.AchievementsContent
import com.sedsoftware.blinkly.compose.ui.exercises.WorkoutContent
import com.sedsoftware.blinkly.compose.ui.garden.GardenContent
import com.sedsoftware.blinkly.compose.ui.home.HomeScreenContent
import com.sedsoftware.blinkly.compose.ui.newreminder.AddNewReminderContent
import com.sedsoftware.blinkly.compose.ui.onboarding.OnboardingContent
import com.sedsoftware.blinkly.compose.ui.preferences.PreferencesContent
import com.sedsoftware.blinkly.compose.ui.extension.asImage
import com.sedsoftware.blinkly.compose.ui.extension.asTitle
import com.sedsoftware.blinkly.compose.ui.widget.BlinklySnackbar
import com.sedsoftware.blinkly.compose.ui.widget.BlinklySnackbarType
import com.sedsoftware.blinkly.compose.ui.widget.BlinklySnackbarVisuals
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.BlinklyNotification
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
    adsConfiguration: BlinklyAdsConfiguration = BlinklyAdsConfiguration.Disabled,
    onAdEvent: (BlinklyAdEvent) -> Unit = {},
    onSystemBarsAppearanceChanged: @Composable (useDarkIcons: Boolean) -> Unit = {},
) {
    val themeState by component.themeState.subscribeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarEvents = remember(component) { mutableStateListOf<SnackbarEvent>() }

    BlinklyAdsHost(
        configuration = adsConfiguration,
        onAdEvent = onAdEvent,
    ) {
        BlinklyAppTheme(
            onSystemBarsAppearanceChanged = onSystemBarsAppearanceChanged,
            themeState = themeState,
        ) {
            LaunchedEffect(component) {
                merge(
                    component.errors.map(SnackbarEvent::Error),
                    component.notifications.map(SnackbarEvent::Notification),
                ).collect { event ->
                    snackbarEvents.add(event)
                }
            }

            snackbarEvents.firstOrNull()?.let { event ->
                val visuals = event.asVisuals()

                LaunchedEffect(event, visuals) {
                    snackbarHostState.showSnackbar(visuals)

                    if (snackbarEvents.firstOrNull() === event) {
                        snackbarEvents.removeAt(0)
                    }
                }
            }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                ChildStack(
                    stack = component.childStack,
                    animation = stackAnimation(
                        animator = fade() + scale(),
                    ),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (val child = it.instance) {
                        is RootComponent.Child.Onboarding -> OnboardingContent(child.component)
                        is RootComponent.Child.HomeScreen -> HomeScreenContent(child.component)
                        is RootComponent.Child.Preferences -> PreferencesContent(child.component)
                        is RootComponent.Child.Workout -> WorkoutContent(child.component)
                        is RootComponent.Child.Achievements -> AchievementsContent(child.component)
                        is RootComponent.Child.Garden -> GardenContent(child.component)
                        is RootComponent.Child.AddNewReminder -> AddNewReminderContent(child.component)
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackbarData ->
                        BlinklySnackbar(snackbarData = snackbarData)
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Top,
                            )
                        )
                        .padding(all = 16.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

private sealed class SnackbarEvent {
    data class Error(val error: BlinklyError) : SnackbarEvent()

    data class Notification(val notification: BlinklyNotification) : SnackbarEvent()
}

@Composable
private fun SnackbarEvent.asVisuals(): BlinklySnackbarVisuals =
    when (this) {
        is SnackbarEvent.Error -> BlinklySnackbarVisuals(
            message = error.asMessage(),
            type = BlinklySnackbarType.ERROR,
        )

        is SnackbarEvent.Notification -> when (val notification = notification) {
            is BlinklyNotification.AchievementUnlocked -> BlinklySnackbarVisuals(
                message = notification.type.asTitle(),
                type = BlinklySnackbarType.ACHIEVEMENT,
                title = stringResource(Res.string.notification_achievement_unlocked),
                icon = notification.type.asImage(),
            )
        }
    }

@Composable
@Suppress("CyclomaticComplexMethod")
private fun BlinklyError.asMessage(): String =
    stringResource(
        resource = syncMessageResource() ?: when (this) {
            is BlinklyError.MainDataLoading -> Res.string.error_main_data_loading
            is BlinklyError.ProgressDataLoading -> Res.string.error_progress_data_loading
            is BlinklyError.TrainingsDataLoading -> Res.string.error_trainings_data_loading
            is BlinklyError.PreferencesLoading -> Res.string.error_preferences_loading
            is BlinklyError.PreferencesSaving -> Res.string.error_preferences_saving
            is BlinklyError.AchievementsLoading -> Res.string.error_achievements_loading
            is BlinklyError.GardenLoading -> Res.string.error_garden_loading
            is BlinklyError.RemindersLoading -> Res.string.error_reminders_loading
            is BlinklyError.ReminderDeleting -> Res.string.error_reminder_deleting
            is BlinklyError.ReminderRestoring -> Res.string.error_reminder_restoring
            is BlinklyError.ReminderCreating -> Res.string.error_reminder_creating
            is BlinklyError.InitialRemindersLoading -> Res.string.error_initial_reminders_loading
            is BlinklyError.InitialRemindersCreating -> Res.string.error_initial_reminders_creating
            is BlinklyError.InitialRemindersClearing -> Res.string.error_initial_reminders_clearing
            is BlinklyError.NotificationPermissionChecking -> Res.string.error_notification_permission_checking
            is BlinklyError.NotificationPermissionRequesting -> Res.string.error_notification_permission_requesting
            is BlinklyError.NotificationPermissionDeniedAlways -> Res.string.error_notification_permission_denied_always
            is BlinklyError.ExactAlarmPermissionChecking -> Res.string.error_exact_alarm_permission_checking
            is BlinklyError.ExactAlarmPermissionRequesting -> Res.string.error_exact_alarm_permission_requesting
            is BlinklyError.ExactAlarmPermissionDenied -> Res.string.error_exact_alarm_permission_denied
            is BlinklyError.RemindersRescheduling -> Res.string.error_reminders_rescheduling
            is BlinklyError.WorkoutDataLoading -> Res.string.error_workout_data_loading
            is BlinklyError.WorkoutSaving -> Res.string.error_workout_saving
            is BlinklyError.Unknown -> Res.string.error_unknown
            else -> Res.string.error_unknown
        }
    )

private fun BlinklyError.syncMessageResource(): StringResource? =
    when (this) {
        is BlinklyError.SyncAuthFailed -> Res.string.error_sync_auth_failed
        is BlinklyError.SyncReadFailed -> Res.string.error_sync_read_failed
        is BlinklyError.SyncWriteFailed -> Res.string.error_sync_write_failed
        is BlinklyError.SyncConflictFailed -> Res.string.error_sync_conflict_failed
        is BlinklyError.SyncUnknown -> Res.string.error_sync_unknown
        else -> null
    }
