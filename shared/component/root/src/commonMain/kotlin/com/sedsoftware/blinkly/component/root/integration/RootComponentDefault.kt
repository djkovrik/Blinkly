package com.sedsoftware.blinkly.component.root.integration

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent
import com.sedsoftware.blinkly.component.achievements.integration.AchievementsComponentDefault
import com.sedsoftware.blinkly.component.workout.WorkoutComponent
import com.sedsoftware.blinkly.component.workout.integration.WorkoutComponentDefault
import com.sedsoftware.blinkly.component.garden.GardenComponent
import com.sedsoftware.blinkly.component.garden.integration.GardenComponentDefault
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.component.home.integration.HomeScreenComponentDefault
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent
import com.sedsoftware.blinkly.component.newreminder.integration.AddNewReminderComponentDefault
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent
import com.sedsoftware.blinkly.component.onboarding.integration.OnboardingComponentDefault
import com.sedsoftware.blinkly.component.preferences.PreferencesComponent
import com.sedsoftware.blinkly.component.preferences.integration.PreferencesComponentDefault
import com.sedsoftware.blinkly.component.root.RootComponent
import com.sedsoftware.blinkly.domain.BlinklyAchievementsWatcher
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyExerciseManager
import com.sedsoftware.blinkly.domain.BlinklyHighlightsProvider
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyBeeper
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklySyncManager
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.BlinklyNotification
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.domain.model.asBlinklyError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Suppress("LongParameterList")
class RootComponentDefault private constructor(
    private val settings: BlinklySettings,
    private val componentContext: ComponentContext,
    private val onboardingComponent: (ComponentContext, (ComponentOutput) -> Unit) -> OnboardingComponent,
    private val homeScreenComponent: (ComponentContext, (ComponentOutput) -> Unit) -> HomeScreenComponent,
    private val preferencesComponent: (ComponentContext, (ComponentOutput) -> Unit) -> PreferencesComponent,
    private val exercisesComponent: (ComponentContext, ExerciseBlock, (ComponentOutput) -> Unit) -> WorkoutComponent,
    private val achievementsComponent: (ComponentContext, (ComponentOutput) -> Unit) -> AchievementsComponent,
    private val gardenComponent: (ComponentContext, (ComponentOutput) -> Unit) -> GardenComponent,
    private val addNewReminderComponent: (ComponentContext, (ComponentOutput) -> Unit) -> AddNewReminderComponent,
    private val achievements: Flow<List<Achievement>>,
    private val achievementUnlockEvents: Flow<AchievementType>,
    mainDispatcher: CoroutineDispatcher,
    private val releaseBeeper: () -> Unit,
) : RootComponent, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        beeper: BlinklyBeeper,
        dispatchers: BlinklyDispatchers,
        notifier: BlinklyNotifier,
        settings: BlinklySettings,
        syncManager: BlinklySyncManager,
        timeUtils: BlinklyTimeUtils,
        achievementsWatcher: BlinklyAchievementsWatcher,
        calendarWatcher: BlinklyCalendarWatcher,
        exerciseManager: BlinklyExerciseManager,
        highlightsProvider: BlinklyHighlightsProvider,
        reminderManager: BlinklyReminderManager,
        treeProgressWatcher: BlinklyTreeProgressWatcher,
    ) : this(
        componentContext = componentContext,
        settings = settings,
        onboardingComponent = { childContext, output ->
            OnboardingComponentDefault(childContext, storeFactory, reminderManager, notifier, dispatchers, output)
        },
        homeScreenComponent = { childContext, output ->
            HomeScreenComponentDefault(
                componentContext = childContext,
                storeFactory = storeFactory,
                dispatchers = dispatchers,
                settings = settings,
                timeUtils = timeUtils,
                achievementsWatcher = achievementsWatcher,
                calendarWatcher = calendarWatcher,
                highlightsProvider = highlightsProvider,
                reminderManager = reminderManager,
                notifier = notifier,
                treeProgressWatcher = treeProgressWatcher,
                homeScreenOutput = output,
            )
        },
        preferencesComponent = { childContext, output ->
            PreferencesComponentDefault(childContext, storeFactory, dispatchers, settings, syncManager, output)
        },
        exercisesComponent = { childContext, block, output ->
            WorkoutComponentDefault(
                componentContext = childContext,
                storeFactory = storeFactory,
                dispatchers = dispatchers,
                block = block,
                exerciseManager = exerciseManager,
                beeper = beeper,
                workoutOutput = output,
            )
        },
        achievementsComponent = { childContext, output ->
            AchievementsComponentDefault(childContext, storeFactory, dispatchers, achievementsWatcher, output)
        },
        gardenComponent = { childContext, output ->
            GardenComponentDefault(childContext, storeFactory, dispatchers, treeProgressWatcher, output)
        },
        addNewReminderComponent = { childContext, output ->
            AddNewReminderComponentDefault(
                componentContext = childContext,
                storeFactory = storeFactory,
                dispatchers = dispatchers,
                reminderManager = reminderManager,
                addNewReminderOutput = output,
            )
        },
        achievements = achievementsWatcher.achievements,
        achievementUnlockEvents = notifier.unlockedAchievements(),
        mainDispatcher = dispatchers.main,
        releaseBeeper = beeper::release,
    )

    private val navigation: StackNavigation<Config> = StackNavigation()
    private val themeStateValue: MutableValue<ThemeState> = MutableValue(settings.themeState)
    private val errorEvents: MutableSharedFlow<BlinklyError> = MutableSharedFlow(extraBufferCapacity = ERROR_BUFFER_CAPACITY)
    private val notificationEvents: MutableSharedFlow<BlinklyNotification> =
        MutableSharedFlow(extraBufferCapacity = NOTIFICATION_BUFFER_CAPACITY)
    private val scope = CoroutineScope(mainDispatcher + SupervisorJob())

    init {
        scope.launch {
            achievements.collect {}
        }
        scope.launch {
            achievementUnlockEvents.collect { type ->
                onChildOutput(
                    ComponentOutput.Common.NotificationReceived(
                        BlinklyNotification.AchievementUnlocked(type),
                    )
                )
            }
        }
        lifecycle.doOnDestroy {
            scope.cancel()
            releaseBeeper()
        }
    }

    private val stack: Value<ChildStack<Config, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = initialConfiguration(),
            handleBackButton = true,
            childFactory = ::createChild,
        )

    override val childStack: Value<ChildStack<*, RootComponent.Child>> = stack
    override val themeState: Value<ThemeState> = themeStateValue
    override val errors = errorEvents.asSharedFlow()
    override val notifications = notificationEvents.asSharedFlow()

    override fun onBack() {
        navigation.pop()
    }

    private fun createChild(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            is Config.Onboarding ->
                RootComponent.Child.Onboarding(onboardingComponent(componentContext, ::onChildOutput))

            is Config.HomeScreen ->
                RootComponent.Child.HomeScreen(homeScreenComponent(componentContext, ::onChildOutput))

            is Config.Preferences ->
                RootComponent.Child.Preferences(preferencesComponent(componentContext, ::onChildOutput))

            is Config.Workout ->
                RootComponent.Child.Workout(exercisesComponent(componentContext, config.block, ::onChildOutput))

            is Config.Achievements ->
                RootComponent.Child.Achievements(achievementsComponent(componentContext, ::onChildOutput))

            is Config.Garden ->
                RootComponent.Child.Garden(gardenComponent(componentContext, ::onChildOutput))

            is Config.AddNewReminder ->
                RootComponent.Child.AddNewReminder(addNewReminderComponent(componentContext, ::onChildOutput))
        }

    private fun onChildOutput(output: ComponentOutput) {
        when (output) {
            is ComponentOutput.Onboarding.GoToHomeScreen -> {
                settings.onboardingDisplayed = true
                navigation.replaceCurrent(Config.HomeScreen)
            }

            is ComponentOutput.Main.OpenPreferences -> {
                navigation.push(Config.Preferences)
            }

            is ComponentOutput.Main.OpenProgressTab -> Unit

            is ComponentOutput.Trainings.OpenExerciseBlock -> {
                navigation.push(Config.Workout(output.block))
            }

            is ComponentOutput.Progress.OpenAchievements -> {
                navigation.push(Config.Achievements)
            }

            is ComponentOutput.Progress.OpenGarden -> {
                navigation.push(Config.Garden)
            }

            is ComponentOutput.Reminders.OpenAddNew -> {
                navigation.push(Config.AddNewReminder)
            }

            is ComponentOutput.Preferences.ThemeStateChanged -> {
                themeStateValue.value = output.value
            }

            is ComponentOutput.Common.BackPressed -> {
                navigation.pop()
            }

            is ComponentOutput.Common.ErrorCaught -> {
                val error = output.throwable.asBlinklyError(BlinklyError::Unknown)
                val cause = error.cause ?: error

                Logger.e(cause) { "Blinkly error caught: ${error.message}" }
                errorEvents.tryEmit(error)
            }

            is ComponentOutput.Common.NotificationReceived -> {
                notificationEvents.tryEmit(output.notification)
            }

            else -> Unit
        }
    }

    private fun initialConfiguration(): Config =
        if (settings.onboardingDisplayed) {
            Config.HomeScreen
        } else {
            Config.Onboarding
        }

    @Serializable
    private sealed interface Config {

        @Serializable
        data object Onboarding : Config

        @Serializable
        data object HomeScreen : Config

        @Serializable
        data object Preferences : Config

        @Serializable
        data class Workout(val block: ExerciseBlock) : Config

        @Serializable
        data object Achievements : Config

        @Serializable
        data object Garden : Config

        @Serializable
        data object AddNewReminder : Config
    }

    private companion object {
        const val ERROR_BUFFER_CAPACITY = 16
        const val NOTIFICATION_BUFFER_CAPACITY = 16
    }
}
