package com.sedsoftware.blinkly.component.step4.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.step4.store.DisclaimerStore.Intent
import com.sedsoftware.blinkly.component.step4.store.DisclaimerStore.State

internal interface DisclaimerStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class OnCheckboxChecked(val checked: Boolean) : Intent
    }

    data class State(
        val checkboxSelected: Boolean = false,
    )
}
