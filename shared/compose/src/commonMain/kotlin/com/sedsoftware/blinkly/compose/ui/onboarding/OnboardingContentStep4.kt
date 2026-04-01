package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.onboarding_disclaimer1
import blinkly.shared.compose.generated.resources.onboarding_disclaimer2
import blinkly.shared.compose.generated.resources.onboarding_disclaimer3
import blinkly.shared.compose.generated.resources.onboarding_disclaimer4
import blinkly.shared.compose.generated.resources.onboarding_disclaimer5
import blinkly.shared.compose.generated.resources.onboarding_disclaimer6
import blinkly.shared.compose.generated.resources.onboarding_disclaimer7
import blinkly.shared.compose.generated.resources.onboarding_disclaimer8
import blinkly.shared.compose.generated.resources.onboarding_disclaimer_got_it
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.step4.OnboardingStep4Component
import com.sedsoftware.blinkly.component.step4.integration.OnboardingStep4ComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingContentStep4(
    component: OnboardingStep4Component,
    modifier: Modifier = Modifier,
) {
    val model: OnboardingStep4Component.Model by component.model.subscribeAsState()
    val scrollState: ScrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(all = 16.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.onboarding_disclaimer1),
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
                text = stringResource(resource = Res.string.onboarding_disclaimer2),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(resource = Res.string.onboarding_disclaimer3),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(resource = Res.string.onboarding_disclaimer4),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(resource = Res.string.onboarding_disclaimer5),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(resource = Res.string.onboarding_disclaimer6),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(resource = Res.string.onboarding_disclaimer7),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(resource = Res.string.onboarding_disclaimer8),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = model.checkboxSelected,
                onCheckedChange = component::onCheckboxSelect,
            )

            Text(
                text = stringResource(resource = Res.string.onboarding_disclaimer_got_it),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        BottomNavigationButtons(
            previousStepAvailable = true,
            nextStepAvailable = true,
            nextStepEnabled = model.checkboxSelected,
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
private fun OnboardingContentStep4PreviewLight() {
    BlinklyWidgetPreview {
        OnboardingContentStep4(OnboardingStep4ComponentPreview(false))
    }
}

@Preview
@Composable
private fun OnboardingContentStep4PreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContentStep4(OnboardingStep4ComponentPreview(true))
    }
}
