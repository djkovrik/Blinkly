package com.sedsoftware.blinkly.component.trainings.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent.Model
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent.TrainingCard
import com.sedsoftware.blinkly.domain.model.ExerciseBlock

class TrainingsTabComponentPreview(
    completedBlocks: Set<ExerciseBlock> = setOf(ExerciseBlock.C),
) : TrainingsTabComponent {

    override val model: Value<Model> = MutableValue(
        Model(
            cards = listOf(
                ExerciseBlock.A,
                ExerciseBlock.C,
                ExerciseBlock.B,
            ).map { block ->
                TrainingCard(
                    block = block,
                    completedToday = block in completedBlocks,
                )
            }
        )
    )

    override fun onBlockAClick() = Unit
    override fun onBlockBClick() = Unit
    override fun onBlockCClick() = Unit
}
