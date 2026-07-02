package com.sedsoftware.blinkly.component.trainings.integration

import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent.Model
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent.TrainingCard
import com.sedsoftware.blinkly.component.trainings.store.TrainingsTabStore.State
import com.sedsoftware.blinkly.domain.model.ExerciseBlock

internal val stateToModel: (State) -> Model = { state ->
    Model(
        cards = listOf(
            ExerciseBlock.A,
            ExerciseBlock.C,
            ExerciseBlock.B,
        ).map { block ->
            TrainingCard(
                block = block,
                completedToday = block in state.completedToday,
            )
        }
    )
}
