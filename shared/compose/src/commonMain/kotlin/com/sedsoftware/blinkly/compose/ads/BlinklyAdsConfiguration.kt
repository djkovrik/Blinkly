package com.sedsoftware.blinkly.compose.ads

enum class BlinklyAdPlacement {
    ACHIEVEMENTS,
    GARDEN,
}

enum class BlinklyAdsPlatform {
    ANDROID,
    IOS,
    PREVIEW,
}

enum class BlinklyAdsBuildType {
    DEBUG,
    RELEASE,
    PREVIEW,
}

data class BlinklyAdsConfiguration(
    val achievementsAdUnitId: String?,
    val gardenAdUnitId: String?,
    val enabledPlacements: Set<BlinklyAdPlacement>,
    val privacyReady: Boolean,
    val platform: BlinklyAdsPlatform,
    val buildType: BlinklyAdsBuildType,
    val appVersion: String,
) {
    fun adUnitId(placement: BlinklyAdPlacement): String? =
        when (placement) {
            BlinklyAdPlacement.ACHIEVEMENTS -> achievementsAdUnitId
            BlinklyAdPlacement.GARDEN -> gardenAdUnitId
        }
            ?.trim()
            ?.takeIf(String::isNotEmpty)

    fun canLoad(placement: BlinklyAdPlacement): Boolean =
        privacyReady && placement in enabledPlacements && adUnitId(placement) != null

    fun hasLoadablePlacement(): Boolean = BlinklyAdPlacement.entries.any(::canLoad)

    companion object {
        val Disabled: BlinklyAdsConfiguration = BlinklyAdsConfiguration(
            achievementsAdUnitId = null,
            gardenAdUnitId = null,
            enabledPlacements = emptySet(),
            privacyReady = false,
            platform = BlinklyAdsPlatform.PREVIEW,
            buildType = BlinklyAdsBuildType.PREVIEW,
            appVersion = "preview",
        )
    }
}

sealed interface BlinklyAdEvent {
    val placement: BlinklyAdPlacement?

    data object InitializationFailed : BlinklyAdEvent {
        override val placement: BlinklyAdPlacement? = null
    }

    data class RequestStarted(override val placement: BlinklyAdPlacement) : BlinklyAdEvent

    data class Loaded(override val placement: BlinklyAdPlacement) : BlinklyAdEvent

    data class LoadFailed(
        override val placement: BlinklyAdPlacement,
        val reason: BlinklyAdLoadFailure,
    ) : BlinklyAdEvent

    data class Impression(override val placement: BlinklyAdPlacement) : BlinklyAdEvent

    data class Clicked(override val placement: BlinklyAdPlacement) : BlinklyAdEvent
}

enum class BlinklyAdLoadFailure {
    NO_FILL,
    NETWORK,
    INTERNAL,
    UNKNOWN,
}
