package com.sedsoftware.blinkly.utils.di

import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.utils.impl.BlinklyTimeUtilsImpl

interface UtilsModule {
    val timeUtils: BlinklyTimeUtils
}

fun UtilsModule(): UtilsModule {
    return object : UtilsModule {
        override val timeUtils: BlinklyTimeUtils by lazy {
            BlinklyTimeUtilsImpl()
        }
    }
}
