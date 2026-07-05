package com.sedsoftware.blinkly.beeper

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

@Suppress("FunctionName")
fun BeeperWrapperFactory(): BeeperWrapper {
    @OptIn(ExperimentalForeignApi::class)
    return object : BeeperWrapper {
        private val players = mutableMapOf<String, AVAudioPlayer>()
        private val fileName = "beep.wav"

        override fun playBeep() {
            val dotIndex = fileName.lastIndexOf('.')
            val name = if (dotIndex >= 0) fileName.substring(0, dotIndex) else fileName
            val ext = if (dotIndex >= 0) fileName.substring(dotIndex + 1) else null

            val player = players[fileName] ?: run {
                val path = NSBundle.mainBundle.pathForResource(
                    name,
                    ofType = ext
                ) ?: return

                val url = NSURL.fileURLWithPath(path)

                AVAudioPlayer(contentsOfURL = url, error = null).also { newPlayer ->
                    newPlayer.prepareToPlay()
                    players[fileName] = newPlayer
                }
            }

            player.currentTime = 0.0
            player.play()
        }

        override fun releasePlayer() {
            players.values.forEach { it.stop() }
            players.clear()
        }
    }
}
