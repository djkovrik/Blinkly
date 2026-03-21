package com.sedsoftware.blinkly.component.trainings.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ExerciseBlock

class TrainingsTabComponentDefault(
    private val componentContext: ComponentContext,
    private val trainingsTabOutput: (ComponentOutput) -> Unit,
) : TrainingsTabComponent, ComponentContext by componentContext {

    override fun onBlockAClick() {
        trainingsTabOutput(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.A))
    }

    override fun onBlockBClick() {
        trainingsTabOutput(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.B))
    }

    override fun onBlockCClick() {
        trainingsTabOutput(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.C))
    }
}
