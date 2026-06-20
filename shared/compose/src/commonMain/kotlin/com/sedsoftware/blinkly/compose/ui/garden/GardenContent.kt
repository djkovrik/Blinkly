package com.sedsoftware.blinkly.compose.ui.garden

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.content_description_back
import blinkly.shared.compose.generated.resources.content_description_tree_preview
import blinkly.shared.compose.generated.resources.garden_all_grown
import blinkly.shared.compose.generated.resources.garden_collection_title
import blinkly.shared.compose.generated.resources.garden_current_tree_title
import blinkly.shared.compose.generated.resources.garden_grown_count
import blinkly.shared.compose.generated.resources.garden_next_tree
import blinkly.shared.compose.generated.resources.garden_stats_title
import blinkly.shared.compose.generated.resources.icon_back
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.garden.GardenComponent
import com.sedsoftware.blinkly.component.garden.integration.GardenComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.asImage
import com.sedsoftware.blinkly.compose.ui.extension.asLabel
import com.sedsoftware.blinkly.compose.ui.extension.clickableOnce
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenContent(
    component: GardenComponent,
    modifier: Modifier = Modifier,
) {
    val model: GardenComponent.Model by component.model.subscribeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(resource = Res.string.garden_current_tree_title),
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
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize(),
        ) {
            item(
                key = "current_tree",
                contentType = "current_tree",
            ) {
                CurrentTreeSection(
                    tree = model.currentTree,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            item(
                key = "garden_stats",
                contentType = "stats",
            ) {
                GardenStatsSection(
                    model = model,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                )
            }

            if (model.grownTrees.isNotEmpty()) {
                item(
                    key = "garden_header",
                    contentType = "header",
                ) {
                    SectionTitle(
                        text = stringResource(resource = Res.string.garden_collection_title),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                items(
                    items = model.grownTrees.chunked(size = 2),
                    key = { row -> "garden_row_${row.first().type.name}" },
                    contentType = { "garden_row" },
                ) { row ->
                    GardenTreeRow(
                        trees = row,
                        onTreeClick = component::onTreeClick,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }

    model.selectedTree?.let { tree ->
        ModalBottomSheet(
            onDismissRequest = component::onTreeDetailsDismiss,
        ) {
            GardenTreeDetailsSheet(
                tree = tree,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            )
        }
    }
}

@Composable
private fun CurrentTreeSection(
    tree: Tree,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(all = 20.dp),
        ) {
            TreeImage(
                tree = tree,
                size = 224,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = tree.type.asLabel(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            TreeProgress(tree = tree)
        }
    }
}

@Composable
private fun TreeProgress(
    tree: Tree,
    modifier: Modifier = Modifier,
) {
    val progressPercent = tree.percentProgress.roundToInt()

    Column(
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = tree.stage.asLabel(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "$progressPercent%",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        LinearProgressIndicator(
            progress = { tree.percentProgress / HUNDRED_PERCENT },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = PROGRESS_TRACK_ALPHA),
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 8.dp)
                .clip(MaterialTheme.shapes.small),
        )
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun GardenTreeRow(
    trees: List<Tree>,
    onTreeClick: (TreeType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        trees.forEach { tree ->
            GardenTreeCard(
                tree = tree,
                onClick = { onTreeClick(tree.type) },
                modifier = Modifier.weight(weight = 1f),
            )
        }

        if (trees.size == 1) {
            Spacer(modifier = Modifier.weight(weight = 1f))
        }
    }
}

@Composable
private fun GardenTreeCard(
    tree: Tree,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val treeName = tree.type.asLabel()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier
            .sizeIn(minHeight = 168.dp)
            .clip(MaterialTheme.shapes.large)
            .clickableOnce(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = treeName
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
        ) {
            TreeImage(
                tree = tree,
                size = 104,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = treeName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GardenStatsSection(
    model: GardenComponent.Model,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.garden_stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(
                    resource = Res.string.garden_grown_count,
                    model.grownTreesCount,
                    model.totalTrees,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = model.nextTreeType?.let { nextType ->
                    stringResource(
                        resource = Res.string.garden_next_tree,
                        nextType.asLabel(),
                        model.daysToNextTree ?: 0,
                    )
                } ?: stringResource(resource = Res.string.garden_all_grown),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun GardenTreeDetailsSheet(
    tree: Tree,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        modifier = modifier,
    ) {
        TreeImage(
            tree = tree,
            size = 256,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = tree.type.asLabel(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TreeImage(
    tree: Tree,
    size: Int,
    modifier: Modifier = Modifier,
) {
    val treeName = tree.type.asLabel()
    val stageName = tree.stage.asLabel()

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.height(height = size.dp),
    ) {
        Image(
            painter = painterResource(resource = tree.asImage()),
            contentDescription = stringResource(
                resource = Res.string.content_description_tree_preview,
                treeName,
                stageName,
            ),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size = size.dp),
        )
    }
}

@Preview(widthDp = 420, heightDp = 1100)
@Composable
private fun GardenContentPreviewLight() {
    BlinklyWidgetPreview {
        GardenContent(component = GardenComponentPreview())
    }
}

@Preview(widthDp = 420, heightDp = 1100)
@Composable
private fun GardenContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        GardenContent(component = GardenComponentPreview())
    }
}

@Preview(widthDp = 420, heightDp = 520)
@Composable
private fun GardenTreeDetailsSheetPreviewLight() {
    BlinklyWidgetPreview {
        GardenTreeDetailsSheet(
            tree = Tree(TreeStage.MAGNIFICENT, TreeType.GINKGO_BILOBA, 100f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        )
    }
}

@Preview(widthDp = 420, heightDp = 520)
@Composable
private fun GardenTreeDetailsSheetPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        GardenTreeDetailsSheet(
            tree = Tree(TreeStage.MAGNIFICENT, TreeType.GINKGO_BILOBA, 100f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        )
    }
}

private const val HUNDRED_PERCENT = 100f
private const val PROGRESS_TRACK_ALPHA = 0.24f
