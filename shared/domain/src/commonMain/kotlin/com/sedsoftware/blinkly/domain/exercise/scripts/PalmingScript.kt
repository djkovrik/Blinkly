package com.sedsoftware.blinkly.domain.exercise.scripts

import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.dsl.exercise
import com.sedsoftware.blinkly.domain.extension.seconds
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ExerciseType

internal fun palmingScript(settings: BlinklySettings): ExerciseScript =
    exercise(ExerciseType.PALMING) {

        timer(settings.palmingDuration.seconds)

        completeBlock()
    }
