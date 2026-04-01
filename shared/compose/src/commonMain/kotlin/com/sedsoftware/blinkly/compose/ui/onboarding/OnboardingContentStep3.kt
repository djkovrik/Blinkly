package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.onboarding_how
import blinkly.shared.compose.generated.resources.onboarding_how_description1
import blinkly.shared.compose.generated.resources.onboarding_how_description2
import blinkly.shared.compose.generated.resources.onboarding_how_description3
import blinkly.shared.compose.generated.resources.onboarding_how_description4
import blinkly.shared.compose.generated.resources.onboarding_how_title1
import blinkly.shared.compose.generated.resources.onboarding_how_title2
import blinkly.shared.compose.generated.resources.onboarding_how_title3
import blinkly.shared.compose.generated.resources.onboarding_how_title4
import com.sedsoftware.blinkly.component.step3.OnboardingStep3Component
import com.sedsoftware.blinkly.component.step3.integration.OnboardingStep3ComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingContentStep3(
    component: OnboardingStep3Component,
    modifier: Modifier = Modifier,
) {
    val scrollState: ScrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(all = 16.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.onboarding_how),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        Column(
            modifier = Modifier
                .weight(weight = 1f, fill = true)
                .verticalScroll(state = scrollState)
        ) {
            Text(
                text = stringResource(resource = Res.string.onboarding_how_title1),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(resource = Res.string.onboarding_how_description1),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(resource = Res.string.onboarding_how_title2),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(resource = Res.string.onboarding_how_description2),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(resource = Res.string.onboarding_how_title3),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(resource = Res.string.onboarding_how_description3),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(resource = Res.string.onboarding_how_title4),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(resource = Res.string.onboarding_how_description4),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        BottomNavigationButtons(
            previousStepAvailable = true,
            nextStepAvailable = true,
            nextStepEnabled = true,
            onPreviousClick = component::onBackClick,
            onNextClick = component::onNextClick,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 32.dp)
                .fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun OnboardingContentStep3PreviewLight() {
    BlinklyWidgetPreview {
        OnboardingContentStep3(OnboardingStep3ComponentPreview())
    }
}

@Preview
@Composable
private fun OnboardingContentStep3PreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContentStep3(OnboardingStep3ComponentPreview())
    }
}
