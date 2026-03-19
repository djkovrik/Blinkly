package com.sedsoftware.blinkly.component.root.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.component.home.integration.HomeScreenComponentDefault
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent
import com.sedsoftware.blinkly.component.onboarding.integration.OnboardingComponentDefault
import com.sedsoftware.blinkly.component.root.RootComponent
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.serialization.Serializable

class RootComponentDefault internal constructor(
    private val settings: BlinklySettings,
    dispatchers: BlinklyDispatchers,
    componentContext: ComponentContext,
    private val onboardingComponent: (ComponentContext, (ComponentOutput) -> Unit) -> OnboardingComponent,
    private val homeScreenComponent: (ComponentContext, (ComponentOutput) -> Unit) -> HomeScreenComponent,
) : RootComponent, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        alarmManager: BlinklyAlarmManager,
        database: BlinklyDatabase,
        dispatchers: BlinklyDispatchers,
        notifier: BlinklyNotifier,
        settings: BlinklySettings,
        timeUtils: BlinklyTimeUtils,
    ) : this(
        componentContext = componentContext,
        dispatchers = dispatchers,
        settings = settings,
        onboardingComponent = { childContext, output ->
            OnboardingComponentDefault()
        },
        homeScreenComponent = { childContext, output ->
            HomeScreenComponentDefault()
        },
    )

    private val navigation: StackNavigation<Config> = StackNavigation()
    private val scope: CoroutineScope = CoroutineScope(dispatchers.main)

    init {
        lifecycle.doOnDestroy {
            scope.cancel()
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

    override fun onBack() {
        navigation.pop()
    }

    private fun createChild(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            is Config.Onboarding ->
                RootComponent.Child.Onboarding(onboardingComponent(componentContext, ::onChildOutput))

            is Config.HomeScreen ->
                RootComponent.Child.HomeScreen(homeScreenComponent(componentContext, ::onChildOutput))
        }

    private fun onChildOutput(output: ComponentOutput) = Unit

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
    }
}
