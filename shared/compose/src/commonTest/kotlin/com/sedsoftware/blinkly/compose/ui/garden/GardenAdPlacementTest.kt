package com.sedsoftware.blinkly.compose.ui.garden

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GardenAdPlacementTest {

    @Test
    fun `garden ad is absent until at least one tree has grown`() {
        assertFalse(shouldPlaceGardenAd(grownTreeCount = 0))
        assertTrue(shouldPlaceGardenAd(grownTreeCount = 1))
        assertTrue(shouldPlaceGardenAd(grownTreeCount = 12))
    }
}
