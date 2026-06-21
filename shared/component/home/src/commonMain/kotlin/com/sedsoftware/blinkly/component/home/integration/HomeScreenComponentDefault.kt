package com.sedsoftware.blinkly.component.home.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.component.home.model.HomeScreenTab
import com.sedsoftware.blinkly.component.main.MainTabComponent
import com.sedsoftware.blinkly.component.main.integration.MainTabComponentDefault
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent
import com.sedsoftware.blinkly.component.progress.integration.ProgressTabComponentDefault
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent
import com.sedsoftware.blinkly.component.reminders.integration.RemindersTabComponentDefault
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent
import com.sedsoftware.blinkly.component.trainings.integration.TrainingsTabComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyHighlightsProvider
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Suppress("LongParameterList")
class HomeScreenComponentDefault private constructor(
    private val componentContext: ComponentContext,
    private val dispatchers: BlinklyDispatchers,
    private val settings: BlinklySettings,
    private val homeScreenOutput: (ComponentOutput) -> Unit,
    private val mainTabComponent: (ComponentContext, (ComponentOutput) -> Unit) -> MainTabComponent,
    private val trainingsTabComponent: (ComponentContext, (ComponentOutput) -> Unit) -> TrainingsTabComponent,
    private val progressTabComponent: (ComponentContext, (ComponentOutput) -> Unit) -> ProgressTabComponent,
    private val remindersTabComponent: (ComponentContext, (ComponentOutput) -> Unit) -> RemindersTabComponent,
) : HomeScreenComponent, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        dispatchers: BlinklyDispatchers,
        settings: BlinklySettings,
        timeUtils: BlinklyTimeUtils,
        calendarWatcher: BlinklyCalendarWatcher,
        highlightsProvider: BlinklyHighlightsProvider,
        reminderManager: BlinklyReminderManager,
        treeProgressWatcher: BlinklyTreeProgressWatcher,
        homeScreenOutput: (ComponentOutput) -> Unit,
    ) : this(
        componentContext = componentContext,
        dispatchers = dispatchers,
        settings = settings,
        homeScreenOutput = homeScreenOutput,
        mainTabComponent = { childContext, componentOutput ->
            MainTabComponentDefault(
                componentContext = childContext,
                storeFactory = storeFactory,
                dispatchers = dispatchers,
                settings = settings,
                timeUtils = timeUtils,
                calendarWatcher = calendarWatcher,
                highlightsProvider = highlightsProvider,
                treeProgressWatcher = treeProgressWatcher,
                mainTabOutput = componentOutput,
            )
        },
        trainingsTabComponent = { childContext, componentOutput ->
            TrainingsTabComponentDefault(childContext, componentOutput)
        },
        progressTabComponent = { childContext, componentOutput ->
            ProgressTabComponentDefault(childContext, componentOutput)
        },
        remindersTabComponent = { childContext, componentOutput ->
            RemindersTabComponentDefault(
                componentContext = childContext,
                storeFactory = storeFactory,
                dispatchers = dispatchers,
                reminderManager = reminderManager,
                remindersTabOutput = componentOutput,
            )
        }
    )

    private val scope: CoroutineScope = CoroutineScope(dispatchers.io)

    init {
        lifecycle.doOnDestroy {
            scope.cancel()
        }

        markOnboardingCompleted()
    }

    private val navigation: StackNavigation<Config> = StackNavigation()

    private val stack: Value<ChildStack<Config, HomeScreenComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.MainTab,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    override val childStack: Value<ChildStack<*, HomeScreenComponent.Child>> = stack

    override fun onTabClick(tab: HomeScreenTab) {
        when (tab) {
            HomeScreenTab.MAIN -> navigation.bringToFront(Config.MainTab)
            HomeScreenTab.TRAINING -> navigation.bringToFront(Config.TrainingsTab)
            HomeScreenTab.PROGRESS -> navigation.bringToFront(Config.ProgressTab)
            HomeScreenTab.REMINDERS -> navigation.bringToFront(Config.RemindersTab)
        }
    }

    private fun onChildOutput(output: ComponentOutput) {
        when (output) {
            is ComponentOutput.Main.OpenProgressTab -> navigation.bringToFront(Config.ProgressTab)
            else -> homeScreenOutput(output)
        }
    }

    private fun createChild(config: Config, componentContext: ComponentContext): HomeScreenComponent.Child =
        when (config) {
            is Config.MainTab ->
                HomeScreenComponent.Child.MainTab(mainTabComponent(componentContext, ::onChildOutput))

            is Config.TrainingsTab ->
                HomeScreenComponent.Child.TrainingsTab(trainingsTabComponent(componentContext, ::onChildOutput))

            is Config.ProgressTab ->
                HomeScreenComponent.Child.ProgressTab(progressTabComponent(componentContext, ::onChildOutput))

            is Config.RemindersTab ->
                HomeScreenComponent.Child.RemindersTab(remindersTabComponent(componentContext, ::onChildOutput))
        }

    private fun markOnboardingCompleted() {
        scope.launch {
            if (!settings.onboardingDisplayed) {
                settings.onboardingDisplayed = true
            }
        }
    }

    @Serializable
    private sealed interface Config {

        @Serializable
        data object MainTab : Config

        @Serializable
        data object TrainingsTab : Config

        @Serializable
        data object ProgressTab : Config

        @Serializable
        data object RemindersTab : Config
    }
}
