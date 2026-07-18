package com.sedsoftware.blinkly.compose.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import co.touchlab.kermit.Logger
import com.sedsoftware.blinkly.compose.ads.BlinklyAdEvent
import com.sedsoftware.blinkly.compose.ads.BlinklyAdsConfiguration
import com.yandex.mobile.ads.kmp.YandexAds
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

internal val LocalBlinklyAdsConfiguration = staticCompositionLocalOf { BlinklyAdsConfiguration.Disabled }
internal val LocalBlinklyAdsSdkReady = staticCompositionLocalOf { false }
internal val LocalBlinklyAdsPreview = staticCompositionLocalOf { false }
internal val LocalBlinklyAdEventListener = staticCompositionLocalOf<(BlinklyAdEvent) -> Unit> { {} }

@Composable
internal fun BlinklyAdsHost(
    configuration: BlinklyAdsConfiguration,
    onAdEvent: (BlinklyAdEvent) -> Unit,
    content: @Composable () -> Unit,
) {
    var sdkReady by remember(configuration) { mutableStateOf(false) }
    val latestOnAdEvent = rememberUpdatedState(onAdEvent)
    val eventListener: (BlinklyAdEvent) -> Unit = remember(configuration) {
        { event ->
            logBlinklyAdEvent(configuration, event)
            latestOnAdEvent.value(event)
        }
    }

    LaunchedEffect(configuration) {
        if (!configuration.hasLoadablePlacement()) return@LaunchedEffect

        runCatching {
            BlinklyAdsRuntime.initialize()
        }.onSuccess {
            sdkReady = true
        }.onFailure { throwable ->
            Logger.e(throwable) { "Yandex Ads SDK initialization failed" }
            eventListener(BlinklyAdEvent.InitializationFailed)
        }
    }

    CompositionLocalProvider(
        LocalBlinklyAdsConfiguration provides configuration,
        LocalBlinklyAdsSdkReady provides sdkReady,
        LocalBlinklyAdEventListener provides eventListener,
        content = content,
    )
}

@Composable
internal fun BlinklyAdsPreviewHost(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalBlinklyAdsConfiguration provides BlinklyAdsConfiguration(
            achievementsAdUnitId = PREVIEW_AD_UNIT_ID,
            gardenAdUnitId = PREVIEW_AD_UNIT_ID,
            enabledPlacements = com.sedsoftware.blinkly.compose.ads.BlinklyAdPlacement.entries.toSet(),
            privacyReady = true,
            platform = com.sedsoftware.blinkly.compose.ads.BlinklyAdsPlatform.PREVIEW,
            buildType = com.sedsoftware.blinkly.compose.ads.BlinklyAdsBuildType.PREVIEW,
            appVersion = "preview",
        ),
        LocalBlinklyAdsPreview provides true,
        content = content,
    )
}

private object BlinklyAdsRuntime {
    private val initializationMutex = Mutex()
    private var initialized: Boolean = false

    suspend fun initialize() {
        initializationMutex.withLock {
            if (initialized) return

            // Blinkly currently serves contextual ads without location or advertising identifiers.
            YandexAds.setUserConsent(false)
            YandexAds.setLocationTracking(false)
            YandexAds.setAgeRestricted(false)
            suspendCancellableCoroutine { continuation ->
                YandexAds.initialize {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }
            initialized = true
        }
    }
}

private fun logBlinklyAdEvent(
    configuration: BlinklyAdsConfiguration,
    event: BlinklyAdEvent,
) {
    val dimensions = "placement=${event.placement?.name?.lowercase() ?: "none"}, " +
        "platform=${configuration.platform.name.lowercase()}, " +
        "buildType=${configuration.buildType.name.lowercase()}, " +
        "appVersion=${configuration.appVersion}"

    when (event) {
        BlinklyAdEvent.InitializationFailed -> Logger.w { "ad_sdk_initialization_failed: $dimensions" }
        is BlinklyAdEvent.RequestStarted -> Logger.d { "ad_request_started: $dimensions" }
        is BlinklyAdEvent.Loaded -> Logger.d { "ad_loaded: $dimensions" }
        is BlinklyAdEvent.LoadFailed -> Logger.w { "ad_load_failed(${event.reason.name.lowercase()}): $dimensions" }
        is BlinklyAdEvent.Impression -> Logger.d { "ad_impression: $dimensions" }
        is BlinklyAdEvent.Clicked -> Logger.d { "ad_clicked: $dimensions" }
    }
}

private const val PREVIEW_AD_UNIT_ID = "preview-only"
