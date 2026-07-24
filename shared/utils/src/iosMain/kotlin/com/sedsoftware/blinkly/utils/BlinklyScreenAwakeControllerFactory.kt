package com.sedsoftware.blinkly.utils

import com.sedsoftware.blinkly.domain.external.BlinklyScreenAwakeController
import platform.UIKit.UIApplication

fun BlinklyScreenAwakeControllerFactory(): BlinklyScreenAwakeController =
    BlinklyScreenAwakeControllerIos()

private class BlinklyScreenAwakeControllerIos : BlinklyScreenAwakeController {

    override fun enable() {
        UIApplication.sharedApplication.idleTimerDisabled = true
    }

    override fun disable() {
        UIApplication.sharedApplication.idleTimerDisabled = false
    }
}
