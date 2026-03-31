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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.app_label
import blinkly.shared.compose.generated.resources.nunito_bold
import blinkly.shared.compose.generated.resources.onboarding_welcome
import blinkly.shared.compose.generated.resources.onboarding_welcome_description
import com.sedsoftware.blinkly.component.step1.OnboardingStep1Component
import com.sedsoftware.blinkly.component.step1.integration.OnboardingStep1ComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingContentStep1(
    component: OnboardingStep1Component,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(all = 16.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.app_label),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.displayLarge,
            fontFamily = FontFamily(Font(resource = Res.font.nunito_bold)),
            modifier = Modifier.padding(top = 64.dp)
        )

        Text(
            text = stringResource(resource = Res.string.onboarding_welcome),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        Text(
            text = stringResource(resource = Res.string.onboarding_welcome_description),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.weight(weight = 1f, fill = true))

        BottomNavigationButtons(
            previousStepAvailable = false,
            nextStepAvailable = true,
            nextStepEnabled = true,
            onPreviousClick = null,
            onNextClick = component::nextStep,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun OnboardingContentStep1PreviewLight() {
    BlinklyWidgetPreview {
        OnboardingContentStep1(OnboardingStep1ComponentPreview())
    }
}

@Preview
@Composable
private fun OnboardingContentStep1PreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContentStep1(OnboardingStep1ComponentPreview())
    }
}
