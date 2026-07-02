package com.sedsoftware.blinkly.component.trainings

import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.domain.model.ExerciseBlock

interface TrainingsTabComponent {

    val model: Value<Model>

    fun onBlockAClick()
    fun onBlockBClick()
    fun onBlockCClick()

    data class Model(
        val cards: List<TrainingCard>,
    )

    data class TrainingCard(
        val block: ExerciseBlock,
        val completedToday: Boolean,
    )
}
