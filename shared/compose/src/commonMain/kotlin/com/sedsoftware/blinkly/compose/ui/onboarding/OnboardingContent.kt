package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.PredictiveBackParams
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.materialPredictiveBackAnimatable
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent

@Composable
fun OnboardingContent(
    component: OnboardingComponent,
    modifier: Modifier = Modifier,
) {
    ChildStack(
        stack = component.childStack,
        animation = stackAnimation(
            animator = slide(),
            predictiveBackParams = {
                PredictiveBackParams(
                    backHandler = component.backHandler,
                    onBack = component::onBack,
                    animatable = ::materialPredictiveBackAnimatable,
                )
            }
        ),
        modifier = modifier.fillMaxSize(),
    ) {
        when (val child = it.instance) {
            is OnboardingComponent.Child.Step1 -> OnboardingContentStep1(child.component)
            is OnboardingComponent.Child.Step2 -> OnboardingContentStep2(child.component)
            is OnboardingComponent.Child.Step3 -> OnboardingContentStep3(child.component)
            is OnboardingComponent.Child.Step4 -> OnboardingContentStep4(child.component)
            is OnboardingComponent.Child.Step5 -> OnboardingContentStep5(child.component)
        }
    }
}
