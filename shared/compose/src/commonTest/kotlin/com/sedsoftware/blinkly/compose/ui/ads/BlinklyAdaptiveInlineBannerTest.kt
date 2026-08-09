package com.sedsoftware.blinkly.compose.ui.ads

import com.sedsoftware.blinkly.compose.ads.BlinklyAdLoadFailure
import com.yandex.mobile.ads.kmp.common.AdRequestError
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
    fun `load failures use stable sdk codes without exposing raw descriptions`() {
        assertEquals(BlinklyAdLoadFailure.INVALID_REQUEST, mapLoadFailure(error(AdRequestError.Code.INVALID_REQUEST)))
        assertEquals(BlinklyAdLoadFailure.NO_FILL, mapLoadFailure(error(AdRequestError.Code.NO_FILL)))
        assertEquals(BlinklyAdLoadFailure.NETWORK, mapLoadFailure(error(AdRequestError.Code.NETWORK_ERROR)))
        assertEquals(BlinklyAdLoadFailure.INTERNAL, mapLoadFailure(error(AdRequestError.Code.INTERNAL_ERROR)))
        assertEquals(BlinklyAdLoadFailure.SYSTEM, mapLoadFailure(error(AdRequestError.Code.SYSTEM_ERROR)))
        assertEquals(BlinklyAdLoadFailure.UNKNOWN, mapLoadFailure(error(AdRequestError.Code.UNKNOWN_ERROR)))
        assertEquals(BlinklyAdLoadFailure.UNKNOWN, mapLoadFailure(error(code = 999)))
    }

    private fun error(code: Int): AdRequestError = AdRequestError(
        code = code,
        description = "raw sdk description must not drive diagnostics",
        adUnitId = "must-not-be-logged",
    )
}
