package com.sedsoftware.blinkly.compose.ui.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.sedsoftware.blinkly.compose.ads.BlinklyAdEvent
import com.sedsoftware.blinkly.compose.ads.BlinklyAdLoadFailure
import com.sedsoftware.blinkly.compose.ads.BlinklyAdPlacement
import com.yandex.mobile.ads.kmp.banner.Banner
import com.yandex.mobile.ads.kmp.banner.BannerAdSize
import com.yandex.mobile.ads.kmp.banner.BannerEvents
import com.yandex.mobile.ads.kmp.banner.rememberBannerAdState
import com.yandex.mobile.ads.kmp.common.AdRequest
import kotlin.math.roundToInt

@Composable
internal fun BlinklyAdaptiveInlineBanner(
    placement: BlinklyAdPlacement,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalBlinklyAdsConfiguration.current
    val adUnitId = configuration.adUnitId(placement)
    when {
        !configuration.canLoad(placement) || adUnitId == null -> Unit
        LocalInspectionMode.current || LocalBlinklyAdsPreview.current ->
            BlinklyAdPreviewPlaceholder(placement = placement, modifier = modifier)
        LocalBlinklyAdsSdkReady.current -> BlinklyLoadedAdaptiveInlineBanner(
            placement = placement,
            adUnitId = adUnitId,
            modifier = modifier,
        )
    }
}

@Composable
private fun BlinklyLoadedAdaptiveInlineBanner(
    placement: BlinklyAdPlacement,
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    val eventListener = LocalBlinklyAdEventListener.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val dimensions = remember(maxWidth) {
            calculateInlineBannerDimensions(availableWidthDp = maxWidth.value)
        }
        val adRequest = remember(adUnitId) { AdRequest(adUnitId = adUnitId) }
        val adSize = remember(dimensions) {
            BannerAdSize.Inline(
                width = dimensions.width.dp,
                maxHeight = dimensions.maxHeight.dp,
            )
        }
        val events = remember(placement, eventListener) {
            BannerEvents(
                onAdLoaded = { eventListener(BlinklyAdEvent.Loaded(placement)) },
                onAdFailedToLoad = { error ->
                    eventListener(
                        BlinklyAdEvent.LoadFailed(
                            placement = placement,
                            reason = normalizeLoadFailure(error.description),
                        )
                    )
                },
                onAdClicked = { eventListener(BlinklyAdEvent.Clicked(placement)) },
                onImpression = { eventListener(BlinklyAdEvent.Impression(placement)) },
            )
        }
        val bannerState = rememberBannerAdState(
            adSize = adSize,
            events = events,
        )

        LaunchedEffect(bannerState, adRequest) {
            eventListener(BlinklyAdEvent.RequestStarted(placement))
            bannerState.loadAd(adRequest)
        }

        Banner(
            state = bannerState,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BlinklyAdPreviewPlaceholder(
    placement: BlinklyAdPlacement,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(PREVIEW_HEIGHT_DP.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PREVIEW_BACKGROUND_ALPHA)),
    ) {
        Text(
            text = "AD PREVIEW · ${placement.name}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

internal data class BlinklyInlineBannerDimensions(
    val width: Int,
    val maxHeight: Int,
)

internal fun calculateInlineBannerDimensions(
    availableWidthDp: Float,
    maxHeightDp: Float = MAX_INLINE_HEIGHT_DP,
): BlinklyInlineBannerDimensions =
    BlinklyInlineBannerDimensions(
        width = availableWidthDp.roundToInt().coerceAtLeast(1),
        maxHeight = maxHeightDp.roundToInt().coerceIn(1, MAX_INLINE_HEIGHT_DP.toInt()),
    )

internal fun normalizeLoadFailure(description: String): BlinklyAdLoadFailure {
    val normalized = description.lowercase()
    return when {
        "no fill" in normalized || "no_fill" in normalized -> BlinklyAdLoadFailure.NO_FILL
        "network" in normalized || "offline" in normalized || "connection" in normalized ->
            BlinklyAdLoadFailure.NETWORK
        "internal" in normalized || "sdk" in normalized -> BlinklyAdLoadFailure.INTERNAL
        else -> BlinklyAdLoadFailure.UNKNOWN
    }
}

private const val MAX_INLINE_HEIGHT_DP = 160f
private const val PREVIEW_HEIGHT_DP = 96
private const val PREVIEW_BACKGROUND_ALPHA = 0.42f
