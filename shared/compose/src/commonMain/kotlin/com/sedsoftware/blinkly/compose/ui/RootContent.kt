package com.sedsoftware.blinkly.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.error_achievements_loading
import blinkly.shared.compose.generated.resources.error_garden_loading
import blinkly.shared.compose.generated.resources.error_initial_reminders_clearing
import blinkly.shared.compose.generated.resources.error_initial_reminders_creating
import blinkly.shared.compose.generated.resources.error_initial_reminders_loading
import blinkly.shared.compose.generated.resources.error_main_data_loading
import blinkly.shared.compose.generated.resources.error_notification_permission_checking
import blinkly.shared.compose.generated.resources.error_notification_permission_requesting
import blinkly.shared.compose.generated.resources.error_preferences_loading
import blinkly.shared.compose.generated.resources.error_preferences_saving
import blinkly.shared.compose.generated.resources.error_progress_data_loading
import blinkly.shared.compose.generated.resources.error_reminder_creating
import blinkly.shared.compose.generated.resources.error_reminder_deleting
import blinkly.shared.compose.generated.resources.error_reminder_restoring
import blinkly.shared.compose.generated.resources.error_reminders_loading
import blinkly.shared.compose.generated.resources.error_trainings_data_loading
import blinkly.shared.compose.generated.resources.error_unknown
import blinkly.shared.compose.generated.resources.error_workout_data_loading
import blinkly.shared.compose.generated.resources.error_workout_saving
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.PredictiveBackParams
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.materialPredictiveBackAnimatable
import com.sedsoftware.blinkly.component.root.RootComponent
import com.sedsoftware.blinkly.compose.theme.BlinklyAppTheme
import com.sedsoftware.blinkly.compose.ui.achievements.AchievementsContent
import com.sedsoftware.blinkly.compose.ui.exercises.WorkoutContent
import com.sedsoftware.blinkly.compose.ui.garden.GardenContent
import com.sedsoftware.blinkly.compose.ui.home.HomeScreenContent
import com.sedsoftware.blinkly.compose.ui.newreminder.AddNewReminderContent
import com.sedsoftware.blinkly.compose.ui.onboarding.OnboardingContent
import com.sedsoftware.blinkly.compose.ui.preferences.PreferencesContent
import com.sedsoftware.blinkly.domain.model.BlinklyError
import org.jetbrains.compose.resources.stringResource

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
    onSystemBarsAppearanceChanged: @Composable (useDarkIcons: Boolean) -> Unit = {},
) {
    val themeState by component.themeState.subscribeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentError: BlinklyError? by remember { mutableStateOf(null) }

    BlinklyAppTheme(
        onSystemBarsAppearanceChanged = onSystemBarsAppearanceChanged,
        themeState = themeState,
    ) {
        LaunchedEffect(component) {
            component.errors.collect { error ->
                currentError = error
            }
        }

        currentError?.let { error ->
            val message = error.asMessage()

            LaunchedEffect(error, message) {
                snackbarHostState.showSnackbar(
                    message = message,
                    withDismissAction = true,
                )

                if (currentError === error) {
                    currentError = null
                }
            }
        }

        Box(
            modifier = modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            ChildStack(
                stack = component.childStack,
                animation = stackAnimation(
                    animator = fade() + scale(),
                    predictiveBackParams = {
                        PredictiveBackParams(
                            backHandler = component.backHandler,
                            onBack = component::onBack,
                            animatable = ::materialPredictiveBackAnimatable,
                        )
                    }
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
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        actionColor = MaterialTheme.colorScheme.onError,
                        dismissActionContentColor = MaterialTheme.colorScheme.onError,
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BlinklyError.asMessage(): String =
    stringResource(
        resource = when (this) {
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
            is BlinklyError.WorkoutDataLoading -> Res.string.error_workout_data_loading
            is BlinklyError.WorkoutSaving -> Res.string.error_workout_saving
            is BlinklyError.Unknown -> Res.string.error_unknown
        }
    )
