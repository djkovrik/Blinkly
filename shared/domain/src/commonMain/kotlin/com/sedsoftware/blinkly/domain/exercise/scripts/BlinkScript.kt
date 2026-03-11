package com.sedsoftware.blinkly.domain.exercise.scripts

import com.sedsoftware.blinkly.domain.exercise.dsl.Blink
import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.dsl.exercise
import com.sedsoftware.blinkly.domain.extension.ms
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ExerciseType

internal fun blinkScript(settings: BlinklySettings): ExerciseScript =
    exercise(ExerciseType.BLINK_BREAK) {

        repeat(settings.blinkBreakCount) {
            Blink(it) every 250.ms
        }

        completeExercise()
    }
