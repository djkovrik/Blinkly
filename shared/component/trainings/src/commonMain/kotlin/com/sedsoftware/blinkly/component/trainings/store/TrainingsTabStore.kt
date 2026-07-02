package com.sedsoftware.blinkly.component.trainings.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.trainings.store.TrainingsTabStore.Intent
import com.sedsoftware.blinkly.component.trainings.store.TrainingsTabStore.Label
import com.sedsoftware.blinkly.component.trainings.store.TrainingsTabStore.State
import com.sedsoftware.blinkly.domain.model.ExerciseBlock

internal interface TrainingsTabStore : Store<Intent, State, Label> {

    sealed interface Intent

    data class State(
        val completedToday: Set<ExerciseBlock> = emptySet(),
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
    }
}
