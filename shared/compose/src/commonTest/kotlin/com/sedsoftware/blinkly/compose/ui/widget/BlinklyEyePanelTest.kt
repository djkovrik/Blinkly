package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.ui.geometry.Offset
import com.sedsoftware.blinkly.domain.model.EyeMovement
import kotlin.test.Test
import kotlin.test.assertEquals

class BlinklyEyePanelTest {

    @Test
    fun `diagonal movements use absolute opposite corners`() {
        assertOffset(-0.7f, -0.7f, EyeMovement.DiagonalTopLeft.diagonalTarget())
        assertOffset(0.7f, 0.7f, EyeMovement.DiagonalBottomRight.diagonalTarget())
        assertOffset(0.7f, -0.7f, EyeMovement.DiagonalTopRight.diagonalTarget())
        assertOffset(-0.7f, 0.7f, EyeMovement.DiagonalBottomLeft.diagonalTarget())
    }

    @Test
    fun `clockwise circle starts at twelve and visits clock quarters`() {
        assertOffset(0f, -1f, EyeMovement.CircleClockwise.pathOffset(0f))
        assertOffset(1f, 0f, EyeMovement.CircleClockwise.pathOffset(0.25f))
        assertOffset(0f, 1f, EyeMovement.CircleClockwise.pathOffset(0.5f))
        assertOffset(-1f, 0f, EyeMovement.CircleClockwise.pathOffset(0.75f))
        assertOffset(0f, -1f, EyeMovement.CircleClockwise.pathOffset(1f))
    }

    @Test
    fun `counterclockwise circle moves left from twelve`() {
        assertOffset(0f, -1f, EyeMovement.CircleCounterclockwise.pathOffset(0f))
        assertOffset(-1f, 0f, EyeMovement.CircleCounterclockwise.pathOffset(0.25f))
    }

    @Test
    fun `figure eight is a closed path`() {
        assertOffset(0f, 0f, EyeMovement.EightClockwise.pathOffset(0f))
        assertOffset(0f, 0f, EyeMovement.EightClockwise.pathOffset(1f))
        assertOffset(0f, 0f, EyeMovement.EightCounterclockwise.pathOffset(0f))
        assertOffset(0f, 0f, EyeMovement.EightCounterclockwise.pathOffset(1f))
    }

    @Test
    fun `movement animation leaves a settle window before the next event`() {
        assertEquals(250, movementAnimationDurationMs(350L))
        assertEquals(900, movementAnimationDurationMs(1_000L))
        assertEquals(3_900, movementAnimationDurationMs(4_000L))
        assertEquals(1, movementAnimationDurationMs(50L))
    }

    private fun assertOffset(expectedX: Float, expectedY: Float, actual: Offset?) {
        requireNotNull(actual)
        assertEquals(expectedX, actual.x, absoluteTolerance = 0.0001f)
        assertEquals(expectedY, actual.y, absoluteTolerance = 0.0001f)
    }
}
