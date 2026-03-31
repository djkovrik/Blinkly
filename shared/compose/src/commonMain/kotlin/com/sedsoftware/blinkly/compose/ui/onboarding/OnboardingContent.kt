package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.PredictiveBackParams
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.materialPredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent
import com.sedsoftware.blinkly.component.onboarding.integration.OnboardingComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview

@Composable
fun OnboardingContent(
    component: OnboardingComponent,
    modifier: Modifier = Modifier,
) {
    val stack by component.childStack.subscribeAsState()
    val activeComponent: OnboardingComponent.Child = stack.active.instance

    val activeIndex: Int = when (activeComponent) {
        is OnboardingComponent.Child.Step1 -> 0
        is OnboardingComponent.Child.Step2 -> 1
        is OnboardingComponent.Child.Step3 -> 2
        is OnboardingComponent.Child.Step4 -> 3
        is OnboardingComponent.Child.Step5 -> 4
    }

    val animatedScrollValue: Float by animateFloatAsState(targetValue = activeIndex.toFloat())

    Box {
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

        PageIndicator(
            pagesCount = TOTAL_PAGES,
            currentScrollPosition = animatedScrollValue,
            indicatorMarkerColor = MaterialTheme.colorScheme.onPrimaryContainer,
            indicatorBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            indicatorSize = 16.dp,
            indicatorSpacing = 8.dp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(alignment = Alignment.BottomCenter),
        )
    }
}

private const val TOTAL_PAGES = 5

@Preview
@Composable
private fun OnboardingContentPreviewStep1Light() {
    BlinklyWidgetPreview {
        OnboardingContent(OnboardingComponentPreview(step = 1))
    }
}

@Preview
@Composable
private fun OnboardingContentPreviewStep2Light() {
    BlinklyWidgetPreview {
        OnboardingContent(OnboardingComponentPreview(step = 2))
    }
}

@Preview
@Composable
private fun OnboardingContentPreviewStep3Light() {
    BlinklyWidgetPreview {
        OnboardingContent(OnboardingComponentPreview(step = 3))
    }
}

@Preview
@Composable
private fun OnboardingContentPreviewStep4Light() {
    BlinklyWidgetPreview {
        OnboardingContent(OnboardingComponentPreview(step = 4))
    }
}

@Preview
@Composable
private fun OnboardingContentPreviewStep5Light() {
    BlinklyWidgetPreview {
        OnboardingContent(OnboardingComponentPreview(step = 5))
    }
}


@Preview
@Composable
private fun OnboardingContentPreviewStep1Dark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContent(OnboardingComponentPreview(step = 1))
    }
}

@Preview
@Composable
private fun OnboardingContentPreviewStep2Dark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContent(OnboardingComponentPreview(step = 2))
    }
}

@Preview
@Composable
private fun OnboardingContentPreviewStep3Dark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContent(OnboardingComponentPreview(step = 3))
    }
}

@Preview
@Composable
private fun OnboardingContentPreviewStep4Dark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContent(OnboardingComponentPreview(step = 4))
    }
}

@Preview
@Composable
private fun OnboardingContentPreviewStep5Dark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContent(OnboardingComponentPreview(step = 5))
    }
}
