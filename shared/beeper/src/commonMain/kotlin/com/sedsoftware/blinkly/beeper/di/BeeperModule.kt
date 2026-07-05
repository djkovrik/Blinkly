package com.sedsoftware.blinkly.beeper.di

import com.sedsoftware.blinkly.beeper.BeeperWrapper
import com.sedsoftware.blinkly.beeper.impl.BlinklyBeeperImpl
import com.sedsoftware.blinkly.domain.external.BlinklyBeeper

interface BeeperModule {
    val beeper: BlinklyBeeper
}

interface BeeperModuleDependencies {
    val wrapper: BeeperWrapper
}

fun BeeperModule(dependencies: BeeperModuleDependencies): BeeperModule {
    return object : BeeperModule {
        override val beeper: BlinklyBeeper by lazy {
            BlinklyBeeperImpl(
                wrapper = dependencies.wrapper,
            )
        }
    }
}
