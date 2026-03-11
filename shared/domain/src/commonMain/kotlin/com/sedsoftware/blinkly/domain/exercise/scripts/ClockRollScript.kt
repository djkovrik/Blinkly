package com.sedsoftware.blinkly.domain.exercise.scripts

import com.sedsoftware.blinkly.domain.exercise.dsl.CircleClockwise
import com.sedsoftware.blinkly.domain.exercise.dsl.CircleCounterclockwise
import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.dsl.exercise
import com.sedsoftware.blinkly.domain.extension.seconds
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ExerciseType

internal fun clockRollScript(settings: BlinklySettings): ExerciseScript =
    exercise(ExerciseType.CLOCK_ROLLS) {

        repeat(settings.clockRollsEachSide) {
            CircleClockwise every 2.seconds
        }

        repeat(settings.clockRollsEachSide) {
            CircleCounterclockwise every 2.seconds
        }

        completeExercise()
    }
