package com.sedsoftware.blinkly.domain.exercise.scripts

import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.dsl.exercise
import com.sedsoftware.blinkly.domain.extension.seconds
import com.sedsoftware.blinkly.domain.model.ExerciseType

internal fun twentyScript(): ExerciseScript =
    exercise(ExerciseType.TWENTY_X3) {

        timer(20.seconds)

        completeBlock()
    }
