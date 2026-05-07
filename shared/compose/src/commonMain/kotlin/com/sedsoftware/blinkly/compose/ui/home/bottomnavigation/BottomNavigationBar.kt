package com.sedsoftware.blinkly.compose.ui.home.bottomnavigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.tab_main
import blinkly.shared.compose.generated.resources.tab_progress
import blinkly.shared.compose.generated.resources.tab_reminders
import blinkly.shared.compose.generated.resources.tab_trainings
import com.sedsoftware.blinkly.component.home.model.HomeScreenTab
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.clickableOnce
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BottomNavigationBar(
    activeTab: HomeScreenTab,
    onTabClick: (HomeScreenTab) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    inactiveContentColor: Color = MaterialTheme.colorScheme.tertiary,
    indicatorSize: Dp = 8.dp,
) {
    val tabs: List<BottomNavigationTab> = remember { bottomNavigationTabs() }

    BoxWithConstraints(
        modifier = modifier
            .background(color = backgroundColor)
            .navigationBarsPadding()
            .height(height = 76.dp),
    ) {
        val itemWidth: Dp = maxWidth / tabs.size
        val indicatorPosition: Float by animateFloatAsState(
            targetValue = activeTab.index.toFloat(),
            animationSpec = tween(durationMillis = 350),
        )

        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 76.dp),
        ) {
            tabs.forEach { tab ->
                BottomNavigationBarItem(
                    tab = tab,
                    isActive = activeTab == tab.type,
                    contentColor = contentColor,
                    inactiveContentColor = inactiveContentColor,
                    onClick = { onTabClick.invoke(tab.type) },
                    modifier = Modifier.weight(weight = 1f),
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(y = 68.dp)
                .fillMaxWidth()
                .height(height = indicatorSize)
                .bottomNavigationWormTransition(
                    position = indicatorPosition,
                    itemWidth = itemWidth,
                    indicatorSize = indicatorSize,
                    color = contentColor,
                ),
        )
    }
}

@Composable
private fun BottomNavigationBarItem(
    tab: BottomNavigationTab,
    isActive: Boolean,
    contentColor: Color,
    inactiveContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize: Dp by animateDpAsState(
        targetValue = if (isActive) 34.dp else 28.dp,
        animationSpec = tween(durationMillis = 150),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .height(height = 76.dp)
            .clip(shape = MaterialTheme.shapes.small)
            .clickableOnce(onClick = onClick)
            .padding(top = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size = 36.dp),
        ) {
            Icon(
                painter = painterResource(resource = tab.iconRes),
                contentDescription = null,
                tint = if (isActive) contentColor else inactiveContentColor,
                modifier = Modifier.size(size = iconSize),
            )
        }

        Spacer(modifier = Modifier.height(height = 6.dp))

        if (isActive) {
            Text(
                text = tab.label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class BottomNavigationTab(
    val type: HomeScreenTab,
    val label: String,
    val iconRes: DrawableResource,
)

private fun bottomNavigationTabs(): List<BottomNavigationTab> =
    listOf(
        BottomNavigationTab(
            type = HomeScreenTab.MAIN,
            label = "Main",
            iconRes = Res.drawable.tab_main,
        ),
        BottomNavigationTab(
            type = HomeScreenTab.TRAININGS,
            label = "Trainings",
            iconRes = Res.drawable.tab_trainings,
        ),
        BottomNavigationTab(
            type = HomeScreenTab.PROGRESS,
            label = "Progress",
            iconRes = Res.drawable.tab_progress,
        ),
        BottomNavigationTab(
            type = HomeScreenTab.REMINDERS,
            label = "Reminders",
            iconRes = Res.drawable.tab_reminders,
        ),
    )

private fun Modifier.bottomNavigationWormTransition(
    position: Float,
    itemWidth: Dp,
    indicatorSize: Dp,
    color: Color,
): Modifier =
    drawBehind {
        val itemWidthPx: Float = itemWidth.toPx()
        val indicatorSizePx: Float = indicatorSize.toPx()
        val currentIndex: Int = position.toInt()
        val progress: Float = position - currentIndex
        val wormOffset: Float = progress * 2f
        val itemStart: Float = currentIndex * itemWidthPx
        val itemCenter: Float = itemStart + itemWidthPx / 2f
        val restingStart: Float = itemCenter - indicatorSizePx / 2f
        val distance: Float = itemWidthPx
        val head: Float = restingStart + distance * 0f.coerceAtLeast(wormOffset - 1f)
        val tail: Float = restingStart + indicatorSizePx + 1f.coerceAtMost(wormOffset) * distance

        val worm = RoundRect(
            left = head,
            top = 0f,
            right = tail,
            bottom = indicatorSizePx,
            cornerRadius = CornerRadius(x = indicatorSizePx / 2f, y = indicatorSizePx / 2f),
        )
        val path = Path().apply { addRoundRect(worm) }

        drawPath(path = path, color = color)
    }

@Preview
@Composable
private fun BottomNavigationBarPreviewLight() {
    BlinklyWidgetPreview {
        BottomNavigationBarPreviewContent()
    }
}

@Preview
@Composable
private fun BottomNavigationBarPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        BottomNavigationBarPreviewContent()
    }
}

@Composable
private fun BottomNavigationBarPreviewContent() {
    var activeTab: HomeScreenTab by remember { mutableStateOf(HomeScreenTab.MAIN) }

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .width(width = 420.dp),
    ) {
        Spacer(modifier = Modifier.height(height = 24.dp))

        BottomNavigationBar(
            activeTab = activeTab,
            onTabClick = { activeTab = it },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
