package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object BlinklySpacing {
    val ScreenHorizontal: Dp = 16.dp
    val ScreenVertical: Dp = 8.dp
    val ScreenContentVertical: Dp = 24.dp
    val SectionGap: Dp = 16.dp
    val HeaderGap: Dp = 8.dp
    val ItemGap: Dp = 10.dp
    val CardPadding: Dp = 16.dp
    val CompactCardPadding: Dp = 12.dp
}

@Composable
internal fun BlinklyScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = BlinklySpacing.HeaderGap),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
internal fun BlinklySectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

@Composable
internal fun BlinklyAppCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    contentPadding: Dp = BlinklySpacing.CardPadding,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(BlinklySpacing.ItemGap),
    content: @Composable ColumnScope.() -> Unit,
) {
    val containerColor = if (highlighted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = if (highlighted) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = if (highlighted) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(width = 1.dp, color = borderColor),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = verticalArrangement,
            modifier = Modifier.padding(all = contentPadding),
            content = content,
        )
    }
}

@Composable
internal fun BlinklySettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    leading: @Composable (RowScope.() -> Unit)? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BlinklySpacing.ItemGap),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (leading != null) {
            leading()
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(weight = 1f),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )

            if (description != null) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
internal fun BlinklyMetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
