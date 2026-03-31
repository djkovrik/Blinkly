package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.onboarding_why
import blinkly.shared.compose.generated.resources.onboarding_why_description1
import blinkly.shared.compose.generated.resources.onboarding_why_description2
import com.sedsoftware.blinkly.component.step2.OnboardingStep2Component
import com.sedsoftware.blinkly.component.step2.integration.OnboardingStep2ComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingContentStep2(
    component: OnboardingStep2Component,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(all = 16.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.onboarding_why),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        Text(
            text = stringResource(resource = Res.string.onboarding_why_description1),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = stringResource(resource = Res.string.onboarding_why_description2),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.weight(weight = 1f, fill = true))

        BottomNavigationButtons(
            previousStepAvailable = true,
            nextStepAvailable = true,
            nextStepEnabled = true,
            onPreviousClick = component::previousStep,
            onNextClick = component::nextStep,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun OnboardingContentStep2PreviewLight() {
    BlinklyWidgetPreview {
        OnboardingContentStep2(OnboardingStep2ComponentPreview())
    }
}

@Preview
@Composable
private fun OnboardingContentStep2PreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContentStep2(OnboardingStep2ComponentPreview())
    }
}
