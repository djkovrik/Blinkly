package com.sedsoftware.blinkly.compose.ui.home.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.achievement_unknown
import blinkly.shared.compose.generated.resources.content_description_locked_achievement
import blinkly.shared.compose.generated.resources.content_description_tree_preview
import blinkly.shared.compose.generated.resources.progress_achievement_unknown
import blinkly.shared.compose.generated.resources.progress_achievements_title
import blinkly.shared.compose.generated.resources.progress_calendar_title
import blinkly.shared.compose.generated.resources.progress_title
import blinkly.shared.compose.generated.resources.progress_tree_level
import blinkly.shared.compose.generated.resources.progress_tree_title
import blinkly.shared.compose.generated.resources.week_friday
import blinkly.shared.compose.generated.resources.week_monday
import blinkly.shared.compose.generated.resources.week_saturday
import blinkly.shared.compose.generated.resources.week_sunday
import blinkly.shared.compose.generated.resources.week_thursday
import blinkly.shared.compose.generated.resources.week_tuesday
import blinkly.shared.compose.generated.resources.week_wednesday
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent.CalendarDay
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent.CalendarDayState
import com.sedsoftware.blinkly.component.progress.integration.ProgressTabComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.asDescription
import com.sedsoftware.blinkly.compose.ui.extension.asImage
import com.sedsoftware.blinkly.compose.ui.extension.asLabel
import com.sedsoftware.blinkly.compose.ui.extension.asTitle
import com.sedsoftware.blinkly.compose.ui.extension.clickableOnce
import com.sedsoftware.blinkly.compose.ui.extension.shimmering
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Tree
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProgressTabContent(
    component: ProgressTabComponent,
    modifier: Modifier = Modifier,
) {
    val model: ProgressTabComponent.Model by component.model.subscribeAsState()

    Column(
        verticalArrangement = Arrangement.spacedBy(space = 18.dp),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 28.dp, bottom = 20.dp),
    ) {
        Text(
            text = stringResource(resource = Res.string.progress_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
        )

        CalendarSection(
            weeks = model.calendarWeeks,
            modifier = Modifier.fillMaxWidth(),
        )

        TreeSection(
            tree = model.tree,
            onClick = component::onGardenClick,
            modifier = Modifier
                .shimmering(visible = model.tree == null, shape = MaterialTheme.shapes.medium)
                .fillMaxWidth(),
        )

        AchievementsSection(
            achievements = model.recentAchievements,
            onClick = component::onAchievementsClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CalendarSection(
    weeks: List<List<CalendarDay?>>,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 10.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.progress_calendar_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            WeekHeader()

            weeks.forEach { week ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    week.forEach { day ->
                        CalendarDayCell(
                            day = day,
                            modifier = Modifier.size(size = 36.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekHeader(
    modifier: Modifier = Modifier,
) {
    val days = listOf(
        stringResource(resource = Res.string.week_monday),
        stringResource(resource = Res.string.week_tuesday),
        stringResource(resource = Res.string.week_wednesday),
        stringResource(resource = Res.string.week_thursday),
        stringResource(resource = Res.string.week_friday),
        stringResource(resource = Res.string.week_saturday),
        stringResource(resource = Res.string.week_sunday),
    )

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        days.forEach { day ->
            Text(
                text = day,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.size(width = 36.dp, height = 22.dp),
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay?,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when (day?.state) {
        CalendarDayState.PERFECT -> MaterialTheme.colorScheme.primary
        CalendarDayState.WORKOUT -> MaterialTheme.colorScheme.primary.copy(alpha = WORKOUT_DAY_ALPHA)
        CalendarDayState.EMPTY,
        null,
            -> MaterialTheme.colorScheme.surfaceContainer
    }
    val contentColor = when (day?.state) {
        CalendarDayState.PERFECT -> MaterialTheme.colorScheme.onPrimary
        CalendarDayState.WORKOUT -> MaterialTheme.colorScheme.primary
        CalendarDayState.EMPTY,
        null,
            -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
    ) {
        Text(
            text = day?.date?.day?.toString().orEmpty(),
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (day?.state == CalendarDayState.PERFECT) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TreeSection(
    tree: Tree?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val treeName = tree?.type?.asLabel().orEmpty()

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp,
        ),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickableOnce(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = treeName
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 10.dp),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.progress_tree_title),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )

            TreeImage(
                tree = tree,
                modifier = Modifier.fillMaxWidth(),
            )

            tree?.let {
                Text(
                    text = stringResource(resource = Res.string.progress_tree_level, it.stage.index, it.type.asLabel()),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TreeImage(
    tree: Tree?,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.height(height = TREE_IMAGE_BOX_HEIGHT.dp),
    ) {
        tree?.let {
            val treeName = it.type.asLabel()
            val stageName = it.stage.asLabel()

            Image(
                painter = painterResource(resource = it.asImage()),
                contentDescription = stringResource(
                    resource = Res.string.content_description_tree_preview,
                    treeName,
                    stageName,
                ),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size = TREE_IMAGE_SIZE.dp),
            )
        }
    }
}

@Composable
private fun AchievementsSection(
    achievements: List<Achievement?>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val achievementsDescription = stringResource(resource = Res.string.progress_achievements_title)

    Column(
        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(resource = Res.string.progress_achievements_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickableOnce(onClick = onClick)
                .semantics {
                    role = Role.Button
                    contentDescription = achievementsDescription
                },
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
                modifier = Modifier.padding(all = 14.dp),
            ) {
                achievements.take(RECENT_ACHIEVEMENTS_COUNT).forEach { achievement ->
                    AchievementPreviewItem(
                        achievement = achievement,
                        modifier = Modifier.weight(weight = 1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementPreviewItem(
    achievement: Achievement?,
    modifier: Modifier = Modifier,
) {
    val title = achievement?.type?.asTitle() ?: stringResource(resource = Res.string.progress_achievement_unknown)
    val subtitle = achievement?.type?.asDescription() ?: stringResource(resource = Res.string.content_description_locked_achievement)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(resource = achievement?.type?.asImage() ?: Res.drawable.achievement_unknown),
            contentDescription = title,
            modifier = Modifier.size(size = 72.dp),
        )

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(widthDp = 420, heightDp = 1060)
@Composable
private fun ProgressTabContentPreviewLight() {
    BlinklyWidgetPreview {
        ProgressTabContent(component = ProgressTabComponentPreview())
    }
}

@Preview(widthDp = 420, heightDp = 1060)
@Composable
private fun ProgressTabContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        ProgressTabContent(component = ProgressTabComponentPreview())
    }
}

private const val WORKOUT_DAY_ALPHA = 0.22f
private const val TREE_IMAGE_BOX_HEIGHT = 184
private const val TREE_IMAGE_SIZE = 176
private const val RECENT_ACHIEVEMENTS_COUNT = 3
