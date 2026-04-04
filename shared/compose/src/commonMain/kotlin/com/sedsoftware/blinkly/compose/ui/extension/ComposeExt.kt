package com.sedsoftware.blinkly.compose.ui.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.week_monday
import blinkly.shared.compose.generated.resources.week_saturday
import blinkly.shared.compose.generated.resources.week_sunday
import blinkly.shared.compose.generated.resources.week_thursday
import blinkly.shared.compose.generated.resources.week_tuesday
import blinkly.shared.compose.generated.resources.week_wednesday
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource

fun Modifier.alsoIf(condition: Boolean, other: Modifier) = if (condition) this.then(other) else this

fun Modifier.clickableOnce(
    onClick: () -> Unit,
    debounceMs: Long = 500L,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    var isEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(isEnabled) {
        if (!isEnabled) {
            delay(debounceMs)
            isEnabled = true
        }
    }

    this.clickable(
        enabled = isEnabled,
        interactionSource = interactionSource,
        indication = ripple(),
        onClick = {
            if (isEnabled) {
                isEnabled = false
                onClick()
            }
        }
    )
}

@Composable
fun DayOfWeek.asLabel(): String =
    when (this) {
        DayOfWeek.MONDAY -> stringResource(Res.string.week_monday)
        DayOfWeek.TUESDAY -> stringResource(Res.string.week_tuesday)
        DayOfWeek.WEDNESDAY -> stringResource(Res.string.week_wednesday)
        DayOfWeek.THURSDAY -> stringResource(Res.string.week_thursday)
        DayOfWeek.FRIDAY -> stringResource(Res.string.week_monday)
        DayOfWeek.SATURDAY -> stringResource(Res.string.week_saturday)
        DayOfWeek.SUNDAY -> stringResource(Res.string.week_sunday)
    }
