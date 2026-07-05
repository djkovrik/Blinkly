package com.sedsoftware.blinkly.domain.exercise.scripts

import com.sedsoftware.blinkly.domain.exercise.dsl.AccommodationClose
import com.sedsoftware.blinkly.domain.exercise.dsl.AccommodationFar
import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.dsl.exercise
import com.sedsoftware.blinkly.domain.extension.seconds
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ExerciseType

internal fun nearFarScript(settings: BlinklySettings): ExerciseScript =
    exercise(ExerciseType.NEAR_FAR_FOCUS) {

        repeat(settings.nearFarFocusCount) { index ->
            if (index > 1) {
                beep()
            }

            AccommodationClose every settings.nearFarFocusDuration.seconds

            beep()

            AccommodationFar every settings.nearFarFocusDuration.seconds
        }

        completeExercise()
    }
