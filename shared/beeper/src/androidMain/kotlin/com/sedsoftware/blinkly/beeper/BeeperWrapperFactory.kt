package com.sedsoftware.blinkly.beeper

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator

@Suppress("FunctionName")
fun BeeperWrapperFactory(context: Context): BeeperWrapper {
    return object : BeeperWrapper {
        private val appContext = context.applicationContext
        private val beepValue: Int = 120
        private val beepFileName = "beep"

        private val toneGenerator = ToneGenerator(
            AudioManager.STREAM_NOTIFICATION,
            90,
        )

        private val soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        private val soundIds = mutableMapOf<String, Int>()
        private val loadedIds = mutableSetOf<Int>()

        init {
            soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) loadedIds += sampleId
            }

            preloadRaw()
        }

        private fun preloadRaw(): Int {
            val resId = appContext.resources.getIdentifier(
                beepFileName,
                "raw",
                appContext.packageName,
            )

            if (resId == 0) return 0

            return soundPool.load(appContext, resId, 1).also { soundId ->
                soundIds[beepFileName] = soundId
            }
        }

        private fun playRaw() {
            val soundId = soundIds[beepFileName] ?: preloadRaw()

            if (soundId != 0 && soundId in loadedIds) {
                soundPool.play(
                    soundId,
                    1f,
                    1f,
                    1,
                    0,
                    1f,
                )
            } else {
                beep()
            }
        }

        private fun beep() {
            toneGenerator.startTone(
                ToneGenerator.TONE_PROP_BEEP,
                beepValue,
            )
        }

        override fun playBeep() {
            playRaw()
        }

        override fun releasePlayer() {
            soundPool.release()
            toneGenerator.release()
        }
    }
}
