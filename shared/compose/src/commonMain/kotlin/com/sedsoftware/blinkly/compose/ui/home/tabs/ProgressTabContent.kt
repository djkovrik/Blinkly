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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyAppCard
import com.sedsoftware.blinkly.compose.ui.widget.BlinklySectionTitle
import com.sedsoftware.blinkly.compose.ui.widget.BlinklySpacing
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
        verticalArrangement = Arrangement.spacedBy(space = BlinklySpacing.SectionGap),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = BlinklySpacing.ScreenHorizontal)
            .padding(top = 28.dp, bottom = 20.dp),
    ) {
        Text(
            text = stringResource(resource = Res.string.progress_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
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
    BlinklyAppCard(
        contentPadding = BlinklySpacing.CompactCardPadding,
        modifier = modifier,
    ) {
        BlinklySectionTitle(
            text = stringResource(resource = Res.string.progress_calendar_title),
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
                        modifier = Modifier.size(size = 34.dp),
                    )
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
    val todayOuterRingColor = MaterialTheme.colorScheme.primary
    val todayInnerRingColor = MaterialTheme.colorScheme.onPrimary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(
                if (day?.isToday == true) {
                    Modifier.drawWithCache {
                        val outerStrokeWidth = TODAY_OUTER_RING_WIDTH.toPx()
                        val innerStrokeWidth = TODAY_INNER_RING_WIDTH.toPx()
                        val cellRadius = size.minDimension / 2f

                        onDrawWithContent {
                            drawContent()
                            drawCircle(
                                color = todayOuterRingColor,
                                radius = cellRadius - outerStrokeWidth / 2f,
                                style = Stroke(width = outerStrokeWidth),
                            )
                            drawCircle(
                                color = todayInnerRingColor,
                                radius = cellRadius - outerStrokeWidth - innerStrokeWidth / 2f,
                                style = Stroke(width = innerStrokeWidth),
                            )
                        }
                    }
                } else {
                    Modifier
                }
            )
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

    BlinklyAppCard(
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
            verticalArrangement = Arrangement.spacedBy(space = BlinklySpacing.ItemGap),
        ) {
            Text(
                text = stringResource(resource = Res.string.progress_tree_title),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
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
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
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
        BlinklySectionTitle(
            text = stringResource(resource = Res.string.progress_achievements_title),
        )

        BlinklyAppCard(
            contentPadding = BlinklySpacing.CompactCardPadding,
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
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
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
    val title = achievement?.type?.asTitle()
    val description = achievement?.type?.asDescription()
        ?: stringResource(resource = Res.string.content_description_locked_achievement)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(resource = achievement?.type?.asImage() ?: Res.drawable.achievement_unknown),
            contentDescription = title ?: description,
            modifier = Modifier.size(size = 60.dp),
        )

        if (title != null) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = if (achievement == null) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.bodySmall
            },
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(widthDp = 420, heightDp = 920)
@Composable
private fun ProgressTabContentPreviewLight() {
    ProgressTabContentPreview(
        todayState = CalendarDayState.EMPTY,
        showLockedAchievements = true,
    )
}

@Preview(widthDp = 420, heightDp = 920)
@Composable
private fun ProgressTabContentPreviewDark() {
    ProgressTabContentPreview(
        todayState = CalendarDayState.EMPTY,
        showLockedAchievements = true,
        isDarkTheme = true,
    )
}

@Preview(widthDp = 420, heightDp = 920)
@Composable
private fun ProgressTabContentPreviewWorkoutLight() {
    ProgressTabContentPreview(todayState = CalendarDayState.WORKOUT)
}

@Preview(widthDp = 420, heightDp = 920)
@Composable
private fun ProgressTabContentPreviewWorkoutDark() {
    ProgressTabContentPreview(todayState = CalendarDayState.WORKOUT, isDarkTheme = true)
}

@Preview(widthDp = 420, heightDp = 920)
@Composable
private fun ProgressTabContentPreviewPerfectLight() {
    ProgressTabContentPreview(todayState = CalendarDayState.PERFECT)
}

@Preview(widthDp = 420, heightDp = 920)
@Composable
private fun ProgressTabContentPreviewPerfectDark() {
    ProgressTabContentPreview(todayState = CalendarDayState.PERFECT, isDarkTheme = true)
}

@Composable
private fun ProgressTabContentPreview(
    todayState: CalendarDayState,
    showLockedAchievements: Boolean = false,
    isDarkTheme: Boolean = false,
) {
    BlinklyWidgetPreview(isDarkTheme = isDarkTheme) {
        val component = if (showLockedAchievements) {
            ProgressTabComponentPreview(
                todayState = todayState,
                recentAchievements = List(size = RECENT_ACHIEVEMENTS_COUNT) { null },
            )
        } else {
            ProgressTabComponentPreview(todayState = todayState)
        }

        ProgressTabContent(component = component)
    }
}

private const val WORKOUT_DAY_ALPHA = 0.22f
private val TODAY_OUTER_RING_WIDTH = 2.dp
private val TODAY_INNER_RING_WIDTH = 1.dp
private const val TREE_IMAGE_BOX_HEIGHT = 148
private const val TREE_IMAGE_SIZE = 140
private const val RECENT_ACHIEVEMENTS_COUNT = 3
