package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_no
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_title
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_twenty_description
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_twenty_title
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_yes
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component
import com.sedsoftware.blinkly.component.step5.integration.OnboardingStep5ComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.newreminder.AddWeeklyPeriodContent
import com.sedsoftware.blinkly.utils.PreviewContent
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingContentStep5(
    component: OnboardingStep5Component,
    modifier: Modifier = Modifier,
) {
    val model: OnboardingStep5Component.Model by component.model.subscribeAsState()
    val scrollState: ScrollState = rememberScrollState()

    val setupOptions: List<String> = listOf(
        stringResource(Res.string.onboarding_initial_setup_no),
        stringResource(Res.string.onboarding_initial_setup_yes),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(all = 16.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.onboarding_initial_setup_twenty_title),
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
                text = stringResource(resource = Res.string.onboarding_initial_setup_twenty_description),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(resource = Res.string.onboarding_initial_setup_title),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(modifier = Modifier
                .selectableGroup()
                .padding(bottom = 16.dp)
            ) {
                setupOptions.forEachIndexed { index, text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = 32.dp)
                            .selectable(
                                selected = when (index) {
                                    0 -> !model.showInitialSetup
                                    1 -> model.showInitialSetup
                                    else -> false
                                },
                                onClick = { component.onInitialSetupChoice(index != 0) },
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = when (index) {
                                0 -> !model.showInitialSetup
                                1 -> model.showInitialSetup
                                else -> false
                            },
                            onClick = null,
                        )
                        Text(
                            text = text,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = model.showInitialSetup,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    initialHeight = { 0 },
                ) + fadeIn(),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    targetHeight = { 0 },
                ) + fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                AddWeeklyPeriodContent(
                    selectedTimeFrom = model.selectedTimeFrom,
                    selectedTimeUntil = model.selectedTimeUntil,
                    selectedDays = model.selectedDays,
                    selectedInterval = model.selectedInterval,
                    createdRemindersCount = model.createdRemindersCount,
                    onTimeFromSelect = component::onSelectTimeFrom,
                    onTimeUntilSelect = component::onSelectTimeUntil,
                    onIntervalSelect = component::onSelectInterval,
                    onDayClick = component::onToggleDay,
                    onCreateClick = component::onCreateReminders,
                    onClearClick = component::onClearReminders,
                )
            }
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

@Preview(widthDp = 440, heightDp = 880)
@Composable
private fun OnboardingContentStep5PreviewLight() {
    BlinklyWidgetPreview {
        OnboardingContentStep5PreviewContent()
    }
}

@Preview(widthDp = 440, heightDp = 880)
@Composable
private fun OnboardingContentStep5PreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        OnboardingContentStep5PreviewContent()
    }
}

@Composable
@PreviewContent
private fun OnboardingContentStep5PreviewContent() {
    OnboardingContentStep5(
        OnboardingStep5ComponentPreview(
            showInitialSetup = true,
            selectedTimeFrom = LocalTime(10, 0),
            selectedTimeUntil = LocalTime(18, 0),
            selectedInterval = 60,
            selectedDays = DayOfWeek.entries.toList(),
            createdRemindersCount = 0,
        )
    )
}
