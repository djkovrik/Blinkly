package com.sedsoftware.blinkly.compose.ui.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlinx.coroutines.delay

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
