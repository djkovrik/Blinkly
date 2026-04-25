package com.sedsoftware.blinkly.component.root.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent
import com.sedsoftware.blinkly.component.achievements.integration.AchievementsComponentDefault
import com.sedsoftware.blinkly.component.blocka.BlockAComponent
import com.sedsoftware.blinkly.component.blocka.integration.BlockAComponentDefault
import com.sedsoftware.blinkly.component.blockb.BlockBComponent
import com.sedsoftware.blinkly.component.blockb.integration.BlockBComponentDefault
import com.sedsoftware.blinkly.component.blockc.BlockCComponent
import com.sedsoftware.blinkly.component.blockc.integration.BlockCComponentDefault
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
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.serialization.Serializable

@Suppress("LongParameterList")
class RootComponentDefault private constructor(
    private val settings: BlinklySettings,
    private val dispatchers: BlinklyDispatchers,
    private val componentContext: ComponentContext,
    private val onboardingComponent: (ComponentContext, (ComponentOutput) -> Unit) -> OnboardingComponent,
    private val homeScreenComponent: (ComponentContext, (ComponentOutput) -> Unit) -> HomeScreenComponent,
    private val preferencesComponent: (ComponentContext, (ComponentOutput) -> Unit) -> PreferencesComponent,
    private val blockAComponent: (ComponentContext, (ComponentOutput) -> Unit) -> BlockAComponent,
    private val blockBComponent: (ComponentContext, (ComponentOutput) -> Unit) -> BlockBComponent,
    private val blockCComponent: (ComponentContext, (ComponentOutput) -> Unit) -> BlockCComponent,
    private val achievementsComponent: (ComponentContext, (ComponentOutput) -> Unit) -> AchievementsComponent,
    private val gardenComponent: (ComponentContext, (ComponentOutput) -> Unit) -> GardenComponent,
    private val addNewReminderComponent: (ComponentContext, (ComponentOutput) -> Unit) -> AddNewReminderComponent,
) : RootComponent, ComponentContext by componentContext {

    @Suppress("UnusedPrivateProperty")
    constructor(
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        alarmManager: BlinklyAlarmManager,
        database: BlinklyDatabase,
        dispatchers: BlinklyDispatchers,
        notifier: BlinklyNotifier,
        settings: BlinklySettings,
        timeUtils: BlinklyTimeUtils,
        achievementsWatcher: BlinklyAchievementsWatcher,
        calendarWatcher: BlinklyCalendarWatcher,
        exerciseManager: BlinklyExerciseManager,
        highlightsProvider: BlinklyHighlightsProvider,
        reminderManager: BlinklyReminderManager,
        treeProgressWatcher: BlinklyTreeProgressWatcher,
    ) : this(
        componentContext = componentContext,
        dispatchers = dispatchers,
        settings = settings,
        onboardingComponent = { childContext, output ->
            OnboardingComponentDefault(childContext, storeFactory, reminderManager, dispatchers, output)
        },
        homeScreenComponent = { childContext, output ->
            HomeScreenComponentDefault(childContext, dispatchers, settings, output)
        },
        preferencesComponent = { childContext, output ->
            PreferencesComponentDefault(childContext, output)
        },
        blockAComponent = { childContext, output ->
            BlockAComponentDefault(childContext, output)
        },
        blockBComponent = { childContext, output ->
            BlockBComponentDefault(childContext, output)
        },
        blockCComponent = { childContext, output ->
            BlockCComponentDefault(childContext, output)
        },
        achievementsComponent = { childContext, output ->
            AchievementsComponentDefault(childContext, output)
        },
        gardenComponent = { childContext, output ->
            GardenComponentDefault(childContext, output)
        },
        addNewReminderComponent = { childContext, output ->
            AddNewReminderComponentDefault(childContext, output)
        },
    )

    private val navigation: StackNavigation<Config> = StackNavigation()
    private val scope: CoroutineScope = CoroutineScope(dispatchers.io)

    private val backCallback: BackCallback = BackCallback { onBack() }

    init {
        lifecycle.doOnDestroy {
            scope.cancel()
        }

        backCallback.isEnabled = false
        backHandler.register(backCallback)
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

            is Config.BlockA ->
                RootComponent.Child.BlockA(blockAComponent(componentContext, ::onChildOutput))

            is Config.BlockB ->
                RootComponent.Child.BlockB(blockBComponent(componentContext, ::onChildOutput))

            is Config.BlockC ->
                RootComponent.Child.BlockC(blockCComponent(componentContext, ::onChildOutput))

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
                navigation.replaceCurrent(Config.HomeScreen)
                backCallback.isEnabled = true
            }

            is ComponentOutput.Main.OpenPreferences -> {
                navigation.push(Config.Preferences)
            }

            is ComponentOutput.Trainings.OpenExerciseBlock -> {
                when (output.block) {
                    ExerciseBlock.A -> navigation.push(Config.BlockA)
                    ExerciseBlock.B -> navigation.push(Config.BlockB)
                    ExerciseBlock.C -> navigation.push(Config.BlockC)
                }
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

            is ComponentOutput.Common.BackPressed -> {
                navigation.pop()
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
        data object BlockA : Config

        @Serializable
        data object BlockB : Config

        @Serializable
        data object BlockC : Config

        @Serializable
        data object Achievements : Config

        @Serializable
        data object Garden : Config

        @Serializable
        data object AddNewReminder : Config
    }
}
