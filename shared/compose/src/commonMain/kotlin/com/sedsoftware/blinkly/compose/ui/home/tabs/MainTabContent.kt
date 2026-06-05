package com.sedsoftware.blinkly.compose.ui.home.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.content_description_settings
import blinkly.shared.compose.generated.resources.cta_start
import blinkly.shared.compose.generated.resources.icon_settings
import blinkly.shared.compose.generated.resources.info_activities
import blinkly.shared.compose.generated.resources.info_activities_breaks
import blinkly.shared.compose.generated.resources.info_activities_exercises
import blinkly.shared.compose.generated.resources.info_activities_relax
import blinkly.shared.compose.generated.resources.info_current_tree
import blinkly.shared.compose.generated.resources.info_days
import blinkly.shared.compose.generated.resources.info_tree_streak
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.main.MainTabComponent
import com.sedsoftware.blinkly.component.main.domain.model.GreetingPeriod
import com.sedsoftware.blinkly.component.main.domain.model.MainCtaState
import com.sedsoftware.blinkly.component.main.integration.MainTabComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.asDescription
import com.sedsoftware.blinkly.compose.ui.extension.asLabel
import com.sedsoftware.blinkly.compose.ui.extension.asTitle
import com.sedsoftware.blinkly.compose.ui.extension.shimmering
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun MainTabContent(
    component: MainTabComponent,
    modifier: Modifier = Modifier,
) {
    val model: MainTabComponent.Model by component.model.subscribeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(
                        onClick = component::onPreferencesClick,
                        modifier = Modifier.padding(all = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(resource = Res.drawable.icon_settings),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = stringResource(resource = Res.string.content_description_settings),
                            modifier = Modifier.size(size = 28.dp)
                        )
                    }
                }
            )
        },
        modifier = modifier.systemBarsPadding()
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
        ) {
            Text(
                text = model.greetingPeriod.asLabel(),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FirstCardTreeGrowth(
                model = model,
                onCtaClick = component::onPrimaryCtaClick,
                modifier = Modifier,
            )

            SecondCardTreeGrowth(
                model = model,
                onTreeClick = component::onTreeClick,
                modifier = Modifier
                    .shimmering(visible = model.tree == null)
                    .fillMaxWidth()
            )

            ThirdCardActivities(
                model = model,
                modifier = Modifier
                    .fillMaxWidth()
            )

            FourthCardHighlights(
                highlight = model.highlight,
                modifier = Modifier
                    .shimmering(visible = model.highlight == null)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FirstCardTreeGrowth(
    model: MainTabComponent.Model,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp,
        ),
        onClick = onCtaClick,
        modifier = modifier,
    ) {
        FirstCardTreeGrowthContent(
            title = model.ctaState.asTitle(),
            description = model.ctaState.asDescription(),
            cta = stringResource(resource = Res.string.cta_start).uppercase(),
            modifier = Modifier.padding(all = 16.dp),
        )
    }
}

@Composable
private fun FirstCardTreeGrowthContent(
    title: String,
    description: String,
    cta: String,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val titleStyle = MaterialTheme.typography.titleMedium
    val descriptionStyle = MaterialTheme.typography.bodyLarge
    val ctaStyle = MaterialTheme.typography.titleSmall
    val titleBottomPadding = 16.dp
    val descriptionBottomPadding = 8.dp
    val inlineCtaMinGap = 8.dp
    val inlineCtaVerticalOffset = 8.dp

    Layout(
        content = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = titleStyle,
            )

            Text(
                text = description,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = descriptionStyle,
            )

            Text(
                text = cta,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                style = ctaStyle,
            )
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val titlePlaceable = measurables[0].measure(looseConstraints)
        val descriptionPlaceable = measurables[1].measure(looseConstraints)
        val ctaPlaceable = measurables[2].measure(looseConstraints)

        val width = maxOf(
            titlePlaceable.width,
            descriptionPlaceable.width,
            ctaPlaceable.width,
            constraints.minWidth,
        ).coerceAtMost(maximumValue = constraints.maxWidth)

        val titleBottomPaddingPx = titleBottomPadding.roundToPx()
        val descriptionBottomPaddingPx = descriptionBottomPadding.roundToPx()
        val inlineCtaMinGapPx = inlineCtaMinGap.roundToPx()
        val inlineCtaVerticalOffsetPx = inlineCtaVerticalOffset.roundToPx()
        val descriptionY = titlePlaceable.height + titleBottomPaddingPx

        val descriptionLayoutResult = textMeasurer.measure(
            text = AnnotatedString(description),
            style = descriptionStyle,
            constraints = looseConstraints,
            layoutDirection = layoutDirection,
        )
        val lastLineIndex = descriptionLayoutResult.lineCount - 1
        val ctaX = width - ctaPlaceable.width
        val canPlaceCtaInline = if (lastLineIndex < 0) {
            false
        } else if (layoutDirection == LayoutDirection.Ltr) {
            descriptionLayoutResult.getLineRight(lastLineIndex).roundToInt() + inlineCtaMinGapPx <= ctaX
        } else {
            ctaPlaceable.width + inlineCtaMinGapPx <= descriptionLayoutResult.getLineLeft(lastLineIndex).roundToInt()
        }

        val ctaY = if (canPlaceCtaInline) {
            val ctaBaseline = ctaPlaceable[FirstBaseline]

            if (ctaBaseline != AlignmentLine.Unspecified) {
                descriptionY +
                        descriptionLayoutResult.getLineBaseline(lastLineIndex).roundToInt() -
                        ctaBaseline +
                        inlineCtaVerticalOffsetPx
            } else {
                descriptionY + descriptionLayoutResult.getLineTop(lastLineIndex).roundToInt() + inlineCtaVerticalOffsetPx
            }
        } else {
            descriptionY + descriptionPlaceable.height + descriptionBottomPaddingPx
        }
        val height = maxOf(
            descriptionY + descriptionPlaceable.height,
            ctaY + ctaPlaceable.height,
            constraints.minHeight,
        ).coerceAtMost(maximumValue = constraints.maxHeight)

        layout(width = width, height = height) {
            titlePlaceable.placeRelative(x = 0, y = 0)
            descriptionPlaceable.placeRelative(x = 0, y = descriptionY)
            ctaPlaceable.placeRelative(x = ctaX, y = ctaY)
        }
    }
}

@Composable
private fun SecondCardTreeGrowth(
    model: MainTabComponent.Model,
    onTreeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp,
        ),
        onClick = onTreeClick,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = stringResource(resource = Res.string.info_current_tree),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = model.tree?.type?.asLabel().orEmpty(),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = model.tree?.stage?.asLabel().orEmpty(),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .alpha(alpha = 0.7f),
            )

            Text(
                text = "${stringResource(resource = Res.string.info_tree_streak)} " +
                        "${model.treeGrowthStreakDays}${stringResource(resource = Res.string.info_days)}",
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ThirdCardActivities(
    model: MainTabComponent.Model,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = stringResource(resource = Res.string.info_activities),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "${stringResource(resource = Res.string.info_activities_exercises)} ${model.exercisesToday}",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "${stringResource(resource = Res.string.info_activities_relax)} ${model.palmingToday}",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "${stringResource(resource = Res.string.info_activities_breaks)} ${model.twentyX3Today}",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LinearProgressIndicator(
                progress = { model.dailyProgressPercent / 100f },
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FourthCardHighlights(
    highlight: HighlightOfTheDay?,
    modifier: Modifier = Modifier,
) {
    val annotatedString = buildAnnotatedString {
        when (highlight) {
            is HighlightOfTheDay.Tip -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                append(highlight.asTitle())
                append(": ")
                pop()
                append(highlight.asDescription())
            }

            is HighlightOfTheDay.Fact -> {
                append(highlight.asDescription())
            }

            else -> Unit
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = highlight?.asTitle().orEmpty(),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = annotatedString,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
@Preview(widthDp = 1200, heightDp = 1800)
private fun MainTabContentPreviewLight() {
    BlinklyWidgetPreview {
        MainTabPreviewContent()
    }
}

@Composable
@Preview(widthDp = 1200, heightDp = 1800)
private fun MainTabContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        MainTabPreviewContent()
    }
}

@Composable
@Suppress("MagicNumber")
private fun MainTabPreviewContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 1.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f)
        ) {
            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.MORNING,
                ctaState = MainCtaState.MorningWarmUp,
                highlight = HighlightOfTheDay.Tip(21),
                tree = Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.DAY,
                ctaState = MainCtaState.WorkBreakDue,
                highlight = HighlightOfTheDay.Fact(48),
                tree = Tree(TreeStage.SMALL, TreeType.GINKGO_BILOBA, 20f),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.DAY,
                ctaState = MainCtaState.AfternoonWarmUp,
                highlight = HighlightOfTheDay.Fact(47),
                tree = Tree(TreeStage.YOUNG, TreeType.MIMOSA_PUDICA, 40f),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(space = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f)
        ) {
            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.EVENING,
                ctaState = MainCtaState.EveningRelax,
                highlight = HighlightOfTheDay.Tip(30),
                tree = Tree(TreeStage.GROWING, TreeType.QUERCUS_ROBUR, 60f),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.EVENING,
                ctaState = MainCtaState.DayClosing,
                highlight = HighlightOfTheDay.Tip(15),
                tree = Tree(TreeStage.STRONG, TreeType.PINUS, 80f),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.EVENING,
                ctaState = MainCtaState.PerfectDay,
                highlight = HighlightOfTheDay.Fact(32),
                tree = Tree(TreeStage.MAGNIFICENT, TreeType.BETULA, 100f),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )
        }
    }
}

@Composable
private fun MainTabPreviewDefault(
    greetingPeriod: GreetingPeriod,
    ctaState: MainCtaState,
    highlight: HighlightOfTheDay?,
    tree: Tree?,
    modifier: Modifier,
) {
    MainTabContent(
        component = MainTabComponentPreview(
            greetingPeriod = greetingPeriod,
            highlight = highlight,
            ctaState = ctaState,
            restMinutesToday = 60,
            exercisesToday = 8,
            twentyX3Today = 2,
            palmingToday = 4,
            dailyProgressPercent = 50,
            tree = tree,
            treeGrowthStreakDays = 5,
        ),
        modifier = modifier,
    )
}

@Composable
private fun Modifier.previewTileBorder(): Modifier =
    border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
