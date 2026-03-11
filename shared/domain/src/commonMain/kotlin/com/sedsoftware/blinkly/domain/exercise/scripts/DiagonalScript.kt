package com.sedsoftware.blinkly.domain.exercise.scripts

import com.sedsoftware.blinkly.domain.exercise.dsl.DiagonalBottomLeft
import com.sedsoftware.blinkly.domain.exercise.dsl.DiagonalBottomRight
import com.sedsoftware.blinkly.domain.exercise.dsl.DiagonalTopLeft
import com.sedsoftware.blinkly.domain.exercise.dsl.DiagonalTopRight
import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.dsl.exercise
import com.sedsoftware.blinkly.domain.extension.seconds
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ExerciseType

internal fun diagonalScript(settings: BlinklySettings): ExerciseScript =
    exercise(ExerciseType.DIAGONAL_GAZES) {

        repeat(settings.diagonalGazesCount) {
            DiagonalTopLeft every 1.seconds

            DiagonalBottomRight every 1.seconds
        }

        repeat(settings.diagonalGazesCount) {
            DiagonalTopRight every 1.seconds

            DiagonalBottomLeft every 1.seconds
        }

        completeBlock()
    }
