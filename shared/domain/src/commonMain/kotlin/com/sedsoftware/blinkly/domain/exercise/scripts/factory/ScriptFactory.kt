package com.sedsoftware.blinkly.domain.exercise.scripts.factory

import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.scripts.blinkScript
import com.sedsoftware.blinkly.domain.exercise.scripts.clockRollScript
import com.sedsoftware.blinkly.domain.exercise.scripts.diagonalScript
import com.sedsoftware.blinkly.domain.exercise.scripts.figureEightScript
import com.sedsoftware.blinkly.domain.exercise.scripts.nearFarScript
import com.sedsoftware.blinkly.domain.exercise.scripts.palmingScript
import com.sedsoftware.blinkly.domain.exercise.scripts.twentyScript
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ExerciseType

internal object ScriptFactory {

    fun create(type: ExerciseType, settings: BlinklySettings): ExerciseScript =
        when (type) {
            ExerciseType.BLINK_BREAK -> blinkScript(settings)
            ExerciseType.NEAR_FAR_FOCUS -> nearFarScript(settings)
            ExerciseType.DIAGONAL_GAZES -> diagonalScript(settings)
            ExerciseType.FIGURE_EIGHT -> figureEightScript(settings)
            ExerciseType.CLOCK_ROLLS -> clockRollScript(settings)
            ExerciseType.PALMING -> palmingScript(settings)
            ExerciseType.TWENTY_X3 -> twentyScript()
        }
}
