package com.sedsoftware.blinkly.compose.ui.ads

import com.sedsoftware.blinkly.compose.ads.BlinklyAdLoadFailure
import kotlin.test.Test
import kotlin.test.assertEquals

class BlinklyAdaptiveInlineBannerTest {

    @Test
    fun `inline size uses available width and caps height`() {
        assertEquals(
            BlinklyInlineBannerDimensions(width = 320, maxHeight = 160),
            calculateInlineBannerDimensions(availableWidthDp = 320f),
        )
        assertEquals(
            BlinklyInlineBannerDimensions(width = 1_024, maxHeight = 160),
            calculateInlineBannerDimensions(availableWidthDp = 1_024f, maxHeightDp = 240f),
        )
        assertEquals(
            BlinklyInlineBannerDimensions(width = 1, maxHeight = 1),
            calculateInlineBannerDimensions(availableWidthDp = 0f, maxHeightDp = 0f),
        )
    }

    @Test
    fun `load failures are normalized without logging raw sdk data`() {
        assertEquals(BlinklyAdLoadFailure.NO_FILL, normalizeLoadFailure("No fill"))
        assertEquals(BlinklyAdLoadFailure.NETWORK, normalizeLoadFailure("Network connection failed"))
        assertEquals(BlinklyAdLoadFailure.INTERNAL, normalizeLoadFailure("SDK internal error"))
        assertEquals(BlinklyAdLoadFailure.UNKNOWN, normalizeLoadFailure("Something else"))
    }
}
