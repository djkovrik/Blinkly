package com.sedsoftware.blinkly.compose.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BlinklyAdsConfigurationTest {

    @Test
    fun `configuration resolves each placement independently`() {
        val configuration = configuration(
            achievementsAdUnitId = " achievements-id ",
            gardenAdUnitId = "garden-id",
        )

        assertEquals("achievements-id", configuration.adUnitId(BlinklyAdPlacement.ACHIEVEMENTS))
        assertEquals("garden-id", configuration.adUnitId(BlinklyAdPlacement.GARDEN))
        assertTrue(configuration.canLoad(BlinklyAdPlacement.ACHIEVEMENTS))
        assertTrue(configuration.canLoad(BlinklyAdPlacement.GARDEN))
    }

    @Test
    fun `blank disabled and privacy pending placements cannot load`() {
        val blankConfiguration = configuration(achievementsAdUnitId = " ")
        val disabledConfiguration = configuration(enabledPlacements = setOf(BlinklyAdPlacement.GARDEN))
        val privacyPendingConfiguration = configuration(privacyReady = false)

        assertNull(blankConfiguration.adUnitId(BlinklyAdPlacement.ACHIEVEMENTS))
        assertFalse(blankConfiguration.canLoad(BlinklyAdPlacement.ACHIEVEMENTS))
        assertFalse(disabledConfiguration.canLoad(BlinklyAdPlacement.ACHIEVEMENTS))
        assertFalse(privacyPendingConfiguration.hasLoadablePlacement())
        assertFalse(BlinklyAdsConfiguration.Disabled.hasLoadablePlacement())
    }

    private fun configuration(
        achievementsAdUnitId: String? = "achievements-id",
        gardenAdUnitId: String? = "garden-id",
        enabledPlacements: Set<BlinklyAdPlacement> = BlinklyAdPlacement.entries.toSet(),
        privacyReady: Boolean = true,
    ): BlinklyAdsConfiguration = BlinklyAdsConfiguration(
        achievementsAdUnitId = achievementsAdUnitId,
        gardenAdUnitId = gardenAdUnitId,
        enabledPlacements = enabledPlacements,
        privacyReady = privacyReady,
        platform = BlinklyAdsPlatform.PREVIEW,
        buildType = BlinklyAdsBuildType.PREVIEW,
        appVersion = "test",
    )
}
