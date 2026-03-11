package com.sedsoftware.blinkly.domain.exercise.scripts

import com.sedsoftware.blinkly.domain.exercise.dsl.EightClockwise
import com.sedsoftware.blinkly.domain.exercise.dsl.EightCounterclockwise
import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.dsl.exercise
import com.sedsoftware.blinkly.domain.extension.seconds
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ExerciseType

internal fun figureEightScript(settings: BlinklySettings): ExerciseScript =
    exercise(ExerciseType.FIGURE_EIGHT) {

        repeat(settings.figureEightCount) {
            EightClockwise every 4.seconds
        }

        repeat(settings.figureEightCount) {
            EightCounterclockwise every 4.seconds
        }

        completeExercise()
    }
