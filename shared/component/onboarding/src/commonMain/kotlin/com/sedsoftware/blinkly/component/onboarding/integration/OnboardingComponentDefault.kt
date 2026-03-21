package com.sedsoftware.blinkly.component.onboarding.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent
import com.sedsoftware.blinkly.component.step1.OnboardingStep1Component
import com.sedsoftware.blinkly.component.step1.integration.OnboardingStep1ComponentDefault
import com.sedsoftware.blinkly.component.step2.OnboardingStep2Component
import com.sedsoftware.blinkly.component.step2.integration.OnboardingStep2ComponentDefault
import com.sedsoftware.blinkly.component.step3.OnboardingStep3Component
import com.sedsoftware.blinkly.component.step3.integration.OnboardingStep3ComponentDefault
import com.sedsoftware.blinkly.component.step4.OnboardingStep4Component
import com.sedsoftware.blinkly.component.step4.integration.OnboardingStep4ComponentDefault
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component
import com.sedsoftware.blinkly.component.step5.integration.OnboardingStep5ComponentDefault
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.serialization.Serializable

class OnboardingComponentDefault private constructor(
    private val componentContext: ComponentContext,
    private val dispatchers: BlinklyDispatchers,
    private val onboardingOutput: (ComponentOutput) -> Unit,
    private val onboardingStep1: (ComponentContext, (ComponentOutput) -> Unit) -> OnboardingStep1Component,
    private val onboardingStep2: (ComponentContext, (ComponentOutput) -> Unit) -> OnboardingStep2Component,
    private val onboardingStep3: (ComponentContext, (ComponentOutput) -> Unit) -> OnboardingStep3Component,
    private val onboardingStep4: (ComponentContext, (ComponentOutput) -> Unit) -> OnboardingStep4Component,
    private val onboardingStep5: (ComponentContext, (ComponentOutput) -> Unit) -> OnboardingStep5Component,
) : OnboardingComponent, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        dispatchers: BlinklyDispatchers,
        onboardingOutput: (ComponentOutput) -> Unit,
    ) : this(
        dispatchers = dispatchers,
        componentContext = componentContext,
        onboardingOutput = onboardingOutput,
        onboardingStep1 = { childContext, output ->
            OnboardingStep1ComponentDefault(childContext, output)
        },
        onboardingStep2 = { childContext, output ->
            OnboardingStep2ComponentDefault(childContext, output)
        },
        onboardingStep3 = { childContext, output ->
            OnboardingStep3ComponentDefault(childContext, output)
        },
        onboardingStep4 = { childContext, output ->
            OnboardingStep4ComponentDefault(childContext, output)
        },
        onboardingStep5 = { childContext, output ->
            OnboardingStep5ComponentDefault(childContext, output)
        },
    )

    private val navigation: StackNavigation<Config> = StackNavigation()
    private val scope: CoroutineScope = CoroutineScope(dispatchers.io)

    init {
        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    private val stack: Value<ChildStack<Config, OnboardingComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Step1,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    override val childStack: Value<ChildStack<*, OnboardingComponent.Child>> = stack

    override fun onBack() {
        if (childStack.active.instance !is OnboardingComponent.Child.Step1) {
            navigation.pop()
        }
    }

    private fun createChild(config: Config, componentContext: ComponentContext): OnboardingComponent.Child =
        when (config) {
            is Config.Step1 ->
                OnboardingComponent.Child.Step1(onboardingStep1(componentContext, ::onChildOutput))

            is Config.Step2 ->
                OnboardingComponent.Child.Step2(onboardingStep2(componentContext, ::onChildOutput))

            is Config.Step3 ->
                OnboardingComponent.Child.Step3(onboardingStep3(componentContext, ::onChildOutput))

            is Config.Step4 ->
                OnboardingComponent.Child.Step4(onboardingStep4(componentContext, ::onChildOutput))

            is Config.Step5 ->
                OnboardingComponent.Child.Step5(onboardingStep5(componentContext, ::onChildOutput))
        }

    private fun onChildOutput(output: ComponentOutput) {
        when (output) {
            is ComponentOutput.Onboarding.GoToStep2 -> navigation.push(Config.Step2)
            is ComponentOutput.Onboarding.GoToStep3 -> navigation.push(Config.Step3)
            is ComponentOutput.Onboarding.GoToStep4 -> navigation.push(Config.Step4)
            is ComponentOutput.Onboarding.GoToStep5 -> navigation.push(Config.Step5)
            is ComponentOutput.Onboarding.GoBack -> navigation.pop()
            else -> onboardingOutput(output)
        }
    }

    @Serializable
    private sealed interface Config {

        @Serializable
        data object Step1 : Config

        @Serializable
        data object Step2 : Config

        @Serializable
        data object Step3 : Config

        @Serializable
        data object Step4 : Config

        @Serializable
        data object Step5 : Config
    }
}
