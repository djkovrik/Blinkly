package com.sedsoftware.blinkly.beeper.impl

import com.sedsoftware.blinkly.beeper.BeeperWrapper
import com.sedsoftware.blinkly.domain.external.BlinklyBeeper

internal class BlinklyBeeperImpl(
    private val wrapper: BeeperWrapper,
) : BlinklyBeeper {
    override fun beep() = wrapper.playBeep()
    override fun release() = wrapper.releasePlayer()
}
