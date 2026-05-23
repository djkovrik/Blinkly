package com.sedsoftware.blinkly.compose.ui.home.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.content_description_settings
import blinkly.shared.compose.generated.resources.icon_settings
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.main.MainTabComponent
import com.sedsoftware.blinkly.component.main.domain.model.GreetingPeriod
import com.sedsoftware.blinkly.component.main.domain.model.MainCtaState
import com.sedsoftware.blinkly.component.main.integration.MainTabComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.asLabel
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
                    IconButton(onClick = component::onPreferencesClick) {
                        Icon(
                            painter = painterResource(Res.drawable.icon_settings),
                            contentDescription = stringResource(Res.string.content_description_settings),
                        )
                    }
                }
            )
        },
        modifier = modifier.systemBarsPadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .padding(all = 16.dp)
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
        ) {
            Text(
                text = model.greetingPeriod.asLabel(),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
@Preview(widthDp = 1600, heightDp = 1200)
private fun MainTabContentPreviewLight() {
    BlinklyWidgetPreview {
        MainTabPreviewContent()
    }
}

@Composable
@Preview(widthDp = 1600, heightDp = 1200)
private fun MainTabContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        MainTabPreviewContent()
    }
}

@Composable
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
                ctaState = MainCtaState.Idle,
                highlight = null,
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.MORNING,
                ctaState = MainCtaState.MorningWarmUp,
                highlight = HighlightOfTheDay.Fact(47),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.DAY,
                ctaState = MainCtaState.WorkBreakDue,
                highlight = HighlightOfTheDay.Fact(48),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.DAY,
                ctaState = MainCtaState.AfternoonWarmUp,
                highlight = HighlightOfTheDay.Tip(21),
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
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.EVENING,
                ctaState = MainCtaState.DayClosing,
                highlight = HighlightOfTheDay.Tip(15),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            MainTabPreviewDefault(
                greetingPeriod = GreetingPeriod.EVENING,
                ctaState = MainCtaState.PerfectDay,
                highlight = HighlightOfTheDay.Fact(32),
                modifier = Modifier
                    .weight(weight = 1f)
                    .previewTileBorder(),
            )

            Spacer(modifier = Modifier.weight(weight = 1f))
        }
    }
}

@Composable
private fun MainTabPreviewDefault(
    greetingPeriod: GreetingPeriod,
    ctaState: MainCtaState,
    highlight: HighlightOfTheDay?,
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
            dailyProgressPercent = 75,
            tree = Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 50f),
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
