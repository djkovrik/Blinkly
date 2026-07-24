package com.sedsoftware.blinkly.utils

import android.view.Window
import android.view.WindowManager
import com.sedsoftware.blinkly.domain.external.BlinklyScreenAwakeController

@Suppress("FunctionNaming")
fun BlinklyScreenAwakeControllerFactory(window: Window): BlinklyScreenAwakeController =
    BlinklyScreenAwakeControllerAndroid(window)

private class BlinklyScreenAwakeControllerAndroid(
    private val window: Window,
) : BlinklyScreenAwakeController {

    override fun enable() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun disable() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
