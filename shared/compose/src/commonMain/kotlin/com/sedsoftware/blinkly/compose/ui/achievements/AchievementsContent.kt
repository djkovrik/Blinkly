@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.compose.ui.achievements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.achievement_unknown
import blinkly.shared.compose.generated.resources.achievements_hidden_description
import blinkly.shared.compose.generated.resources.achievements_locked
import blinkly.shared.compose.generated.resources.achievements_screen_title
import blinkly.shared.compose.generated.resources.achievements_unlocked
import blinkly.shared.compose.generated.resources.achievements_unlocked_at
import blinkly.shared.compose.generated.resources.content_description_back
import blinkly.shared.compose.generated.resources.content_description_locked_achievement
import blinkly.shared.compose.generated.resources.icon_back
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent.AchievementItem
import com.sedsoftware.blinkly.component.achievements.integration.AchievementsComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.alsoIf
import com.sedsoftware.blinkly.compose.ui.extension.asDescription
import com.sedsoftware.blinkly.compose.ui.extension.asImage
import com.sedsoftware.blinkly.compose.ui.extension.asLabel
import com.sedsoftware.blinkly.compose.ui.extension.asTitle
import com.sedsoftware.blinkly.compose.ui.extension.clickableOnce
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsContent(
    component: AchievementsComponent,
    modifier: Modifier = Modifier,
) {
    val model: AchievementsComponent.Model by component.model.subscribeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(resource = Res.string.achievements_screen_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = component::onBackClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(resource = Res.drawable.icon_back),
                            contentDescription = stringResource(resource = Res.string.content_description_back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(size = 24.dp)
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize(),
        ) {
            model.sections.forEach { section ->
                item(
                    key = "header_${section.level.name}",
                    contentType = "header",
                ) {
                    AchievementSectionHeader(
                        title = section.level.asLabel(),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                items(
                    items = section.achievements,
                    key = { it.type.name },
                    contentType = { "achievement" },
                ) { achievement ->
                    AchievementRow(
                        achievement = achievement,
                        onClick = { component.onAchievementClick(achievement.type) },
                    )
                }
            }
        }
    }

    model.selectedAchievement?.let { achievement ->
        ModalBottomSheet(
            onDismissRequest = component::onDetailsDismiss,
        ) {
            AchievementDetailsSheet(
                achievement = achievement,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            )
        }
    }
}

@Composable
private fun AchievementSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    )
}

private const val LOCKED_ACHIEVEMENT_ALPHA = 0.82f

@Composable
private fun AchievementRow(
    achievement: AchievementItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = achievement.type.asTitle()
    val description = achievement.type.asDescription()
    val lockedDescription = stringResource(resource = Res.string.content_description_locked_achievement)

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 96.dp)
            .alpha(if (achievement.isUnlocked) 1f else LOCKED_ACHIEVEMENT_ALPHA)
            .alsoIf(
                achievement.isDetailsAvailable,
                Modifier
                    .clip(MaterialTheme.shapes.large)
                    .clickableOnce(onClick = onClick)
                    .semantics {
                        role = Role.Button
                        contentDescription = title
                    }
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Image(
                painter = painterResource(
                    resource = if (achievement.isUnlocked) achievement.type.asImage() else Res.drawable.achievement_unknown
                ),
                contentDescription = if (achievement.isUnlocked) title else lockedDescription,
                modifier = Modifier.size(size = 64.dp),
            )

            Spacer(modifier = Modifier.width(width = 14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(space = 6.dp),
                modifier = Modifier.weight(weight = 1f),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(weight = 1f, fill = false),
                    )

                    AchievementStatusChip(isUnlocked = achievement.isUnlocked)
                }

                if (achievement.isDescriptionHidden) {
                    HiddenDescriptionPlaceholder()
                } else {
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementStatusChip(
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (isUnlocked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        contentColor = if (isUnlocked) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = MaterialTheme.shapes.large,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(height = 30.dp)
                .padding(horizontal = 10.dp),
        ) {
            Text(
                text = stringResource(
                    resource = if (isUnlocked) Res.string.achievements_unlocked else Res.string.achievements_locked
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HiddenDescriptionPlaceholder(
    modifier: Modifier = Modifier,
) {
    val description = stringResource(resource = Res.string.achievements_hidden_description)

    Column(
        verticalArrangement = Arrangement.spacedBy(space = 6.dp),
        modifier = modifier.semantics {
            contentDescription = description
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.86f)
                .height(height = 12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.58f)
                .height(height = 12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun AchievementDetailsSheet(
    achievement: AchievementItem,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        modifier = modifier,
    ) {
        val title = achievement.type.asTitle()

        Image(
            painter = painterResource(resource = achievement.type.asImage()),
            contentDescription = title,
            modifier = Modifier.size(size = 256.dp),
        )

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = achievement.type.asDescription(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )

        achievement.unlockedAt?.let { unlockedAt ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.large,
            ) {
                Text(
                    text = stringResource(
                        resource = Res.string.achievements_unlocked_at,
                        unlockedAt.asUserTimeZoneDateTime(),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

private fun Instant.asUserTimeZoneDateTime(): String {
    val localDateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val date = localDateTime.date
    val time = localDateTime.time
    return "${date.year}-${(date.month.ordinal + 1).twoDigits()}-${date.day.twoDigits()} " +
        "${time.hour.twoDigits()}:${time.minute.twoDigits()}"
}

private fun Int.twoDigits(): String = toString().padStart(length = 2, padChar = '0')

@Preview(widthDp = 420, heightDp = 1200)
@Composable
private fun AchievementsContentPreviewLight() {
    BlinklyWidgetPreview {
        AchievementsContent(component = AchievementsComponentPreview())
    }
}

@Preview(widthDp = 420, heightDp = 1200)
@Composable
private fun AchievementsContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        AchievementsContent(component = AchievementsComponentPreview())
    }
}

@Preview(widthDp = 420, heightDp = 520)
@Composable
private fun AchievementDetailsSheetPreviewLight() {
    BlinklyWidgetPreview {
        AchievementDetailsSheet(
            achievement = previewDetailsAchievement(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        )
    }
}

@Preview(widthDp = 420, heightDp = 520)
@Composable
private fun AchievementDetailsSheetPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        AchievementDetailsSheet(
            achievement = previewDetailsAchievement(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        )
    }
}

private fun previewDetailsAchievement(): AchievementItem =
    AchievementItem(
        type = AchievementType.DIAMOND_EYES,
        level = AchievementLevel.INTERMEDIATE,
        unlockedAt = Instant.parse("2026-03-15T18:30:00Z"),
        isUnlocked = true,
        isDetailsAvailable = true,
        isDescriptionHidden = false,
    )
