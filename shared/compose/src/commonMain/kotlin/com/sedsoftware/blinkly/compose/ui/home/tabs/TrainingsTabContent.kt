package com.sedsoftware.blinkly.compose.ui.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.content_description_training_completed
import blinkly.shared.compose.generated.resources.cta_repeat
import blinkly.shared.compose.generated.resources.cta_start
import blinkly.shared.compose.generated.resources.icon_done
import blinkly.shared.compose.generated.resources.tab_trainings
import blinkly.shared.compose.generated.resources.training_evening_relax_benefit
import blinkly.shared.compose.generated.resources.training_evening_relax_description
import blinkly.shared.compose.generated.resources.training_evening_relax_duration
import blinkly.shared.compose.generated.resources.training_evening_relax_title
import blinkly.shared.compose.generated.resources.training_quick_twenty_benefit
import blinkly.shared.compose.generated.resources.training_quick_twenty_description
import blinkly.shared.compose.generated.resources.training_quick_twenty_duration
import blinkly.shared.compose.generated.resources.training_quick_twenty_title
import blinkly.shared.compose.generated.resources.training_status_completed_today
import blinkly.shared.compose.generated.resources.training_workplace_warmup_benefit
import blinkly.shared.compose.generated.resources.training_workplace_warmup_description
import blinkly.shared.compose.generated.resources.training_workplace_warmup_duration
import blinkly.shared.compose.generated.resources.training_workplace_warmup_title
import blinkly.shared.compose.generated.resources.trainings_subtitle
import blinkly.shared.compose.generated.resources.trainings_title
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent.TrainingCard
import com.sedsoftware.blinkly.component.trainings.integration.TrainingsTabComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyButton
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TrainingsTabContent(
    component: TrainingsTabComponent,
    modifier: Modifier = Modifier,
) {
    val model: TrainingsTabComponent.Model by component.model.subscribeAsState()

    Column(
        verticalArrangement = Arrangement.spacedBy(space = 18.dp),
        modifier = modifier
            .systemBarsPadding()
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        TrainingsHeader()

        model.cards.forEach { card ->
            TrainingCatalogCard(
                card = card,
                onClick = {
                    when (card.block) {
                        ExerciseBlock.A -> component.onBlockAClick()
                        ExerciseBlock.B -> component.onBlockBClick()
                        ExerciseBlock.C -> component.onBlockCClick()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TrainingsHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(resource = Res.string.trainings_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = stringResource(resource = Res.string.trainings_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun TrainingCatalogCard(
    card: TrainingCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = card.block.trainingCopy()
    val colors = card.block.trainingColors()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = MaterialTheme.shapes.large,
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 22.dp)
        ) {
            TrainingIcon(
                containerColor = colors.containerColor,
                contentColor = colors.contentColor,
            )

            Text(
                text = copy.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = copy.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            BenefitBadge(
                text = copy.benefit,
                containerColor = colors.containerColor,
                contentColor = colors.contentColor,
            )

            Text(
                text = copy.duration,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            BlinklyButton(
                text = stringResource(
                    resource = if (card.completedToday) {
                        Res.string.cta_repeat
                    } else {
                        Res.string.cta_start
                    }
                ),
                textColor = MaterialTheme.colorScheme.onPrimary,
                buttonColor = MaterialTheme.colorScheme.primary,
                buttonHeight = 50.dp,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            )

            if (card.completedToday) {
                CompletedTodayStatus()
            }
        }
    }
}

@Composable
private fun TrainingIcon(
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size = 58.dp)
            .clip(shape = MaterialTheme.shapes.extraLarge)
            .background(color = containerColor, shape = MaterialTheme.shapes.extraLarge),
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.tab_trainings),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(size = 30.dp),
        )
    }
}

@Composable
private fun BenefitBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.extraLarge)
            .background(color = containerColor, shape = MaterialTheme.shapes.extraLarge)
            .padding(horizontal = 18.dp, vertical = 9.dp),
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CompletedTodayStatus(
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(height = 24.dp),
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.icon_done),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
            contentDescription = stringResource(resource = Res.string.content_description_training_completed),
            modifier = Modifier.size(size = 16.dp),
        )

        Text(
            text = stringResource(resource = Res.string.training_status_completed_today),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ExerciseBlock.trainingCopy(): TrainingCopy =
    when (this) {
        ExerciseBlock.A -> TrainingCopy(
            title = stringResource(resource = Res.string.training_workplace_warmup_title),
            description = stringResource(resource = Res.string.training_workplace_warmup_description),
            benefit = stringResource(resource = Res.string.training_workplace_warmup_benefit),
            duration = stringResource(resource = Res.string.training_workplace_warmup_duration),
        )

        ExerciseBlock.C -> TrainingCopy(
            title = stringResource(resource = Res.string.training_quick_twenty_title),
            description = stringResource(resource = Res.string.training_quick_twenty_description),
            benefit = stringResource(resource = Res.string.training_quick_twenty_benefit),
            duration = stringResource(resource = Res.string.training_quick_twenty_duration),
        )

        ExerciseBlock.B -> TrainingCopy(
            title = stringResource(resource = Res.string.training_evening_relax_title),
            description = stringResource(resource = Res.string.training_evening_relax_description),
            benefit = stringResource(resource = Res.string.training_evening_relax_benefit),
            duration = stringResource(resource = Res.string.training_evening_relax_duration),
        )
    }

@Composable
private fun ExerciseBlock.trainingColors(): TrainingColors =
    when (this) {
        ExerciseBlock.A -> TrainingColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        ExerciseBlock.C -> TrainingColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        ExerciseBlock.B -> TrainingColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }

private data class TrainingCopy(
    val title: String,
    val description: String,
    val benefit: String,
    val duration: String,
)

private data class TrainingColors(
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
@Preview(widthDp = 600, heightDp = 1200)
private fun TrainingsTabContentPreviewLight() {
    BlinklyWidgetPreview {
        TrainingsTabContent(
            component = TrainingsTabComponentPreview(),
        )
    }
}

@Composable
@Preview(widthDp = 600, heightDp = 1200)
private fun TrainingsTabContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        TrainingsTabContent(
            component = TrainingsTabComponentPreview(
                completedBlocks = setOf(
                    ExerciseBlock.A,
                    ExerciseBlock.B,
                )
            ),
        )
    }
}
