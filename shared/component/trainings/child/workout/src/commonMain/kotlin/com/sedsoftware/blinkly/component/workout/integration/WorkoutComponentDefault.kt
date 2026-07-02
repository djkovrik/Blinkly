package com.sedsoftware.blinkly.component.workout.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.workout.WorkoutComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ExerciseBlock

class WorkoutComponentDefault(
    private val componentContext: ComponentContext,
    private val block: ExerciseBlock,
    private val exercisesOutput: (ComponentOutput) -> Unit,
) : WorkoutComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        exercisesOutput(ComponentOutput.Common.BackPressed)
    }
}
