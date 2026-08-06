package com.sedsoftware.blinkly.compose.ui.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyAppCard
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyButton
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyScreenHeader
import com.sedsoftware.blinkly.compose.ui.widget.BlinklySpacing
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
        verticalArrangement = Arrangement.spacedBy(space = BlinklySpacing.SectionGap),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(
                horizontal = BlinklySpacing.ScreenHorizontal,
                vertical = BlinklySpacing.ScreenContentVertical,
            )
    ) {
        BlinklyScreenHeader(
            title = stringResource(resource = Res.string.trainings_title),
            subtitle = stringResource(resource = Res.string.trainings_subtitle),
        )

        model.cards.forEachIndexed { index, card ->
            TrainingCatalogCard(
                card = card,
                isPrimaryAction = index == 0 && !card.completedToday,
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
private fun TrainingCatalogCard(
    card: TrainingCard,
    isPrimaryAction: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = card.block.trainingCopy()
    val colors = card.block.trainingColors()

    BlinklyAppCard(
        contentPadding = BlinklySpacing.CardPadding,
        verticalArrangement = Arrangement.spacedBy(space = 12.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TrainingIcon(
                containerColor = colors.containerColor,
                contentColor = colors.contentColor,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                modifier = Modifier.weight(weight = 1f),
            ) {
                Text(
                    text = copy.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 6.dp),
                ) {
                    MetadataBadge(text = copy.duration)
                    MetadataBadge(
                        text = copy.benefit,
                        containerColor = colors.containerColor,
                        contentColor = colors.contentColor,
                    )
                    if (card.completedToday) {
                        CompletedTodayStatus()
                    }
                }

                Text(
                    text = copy.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        BlinklyButton(
            text = stringResource(
                resource = if (card.completedToday) {
                    Res.string.cta_repeat
                } else {
                    Res.string.cta_start
                }
            ),
            textColor = if (isPrimaryAction) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
            buttonColor = if (isPrimaryAction) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            buttonHeight = 44.dp,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        )
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
            .clip(shape = MaterialTheme.shapes.medium)
            .background(color = containerColor, shape = MaterialTheme.shapes.medium),
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
private fun MetadataBadge(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun CompletedTodayStatus(
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.icon_done),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = stringResource(resource = Res.string.content_description_training_completed),
                modifier = Modifier.size(size = 14.dp),
            )

            Text(
                text = stringResource(resource = Res.string.training_status_completed_today),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
@Preview(widthDp = 420, heightDp = 800)
private fun TrainingsTabContentPreviewLight() {
    BlinklyWidgetPreview {
        TrainingsTabContent(
            component = TrainingsTabComponentPreview(),
        )
    }
}

@Composable
@Preview(widthDp = 420, heightDp = 800, locale = "ru")
private fun TrainingsTabContentPreviewRuLight() {
    BlinklyWidgetPreview {
        TrainingsTabContent(
            component = TrainingsTabComponentPreview(),
        )
    }
}

@Composable
@Preview(widthDp = 420, heightDp = 800)
private fun TrainingsTabContentPreviewDark() {
    BlinklyWidgetPreview(isDarkTheme = true) {
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
