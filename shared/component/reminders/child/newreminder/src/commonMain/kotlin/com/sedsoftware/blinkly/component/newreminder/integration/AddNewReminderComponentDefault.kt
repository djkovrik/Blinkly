package com.sedsoftware.blinkly.component.newreminder.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class AddNewReminderComponentDefault(
    private val componentContext: ComponentContext,
    private val addNewReminderOutput: (ComponentOutput) -> Unit,
) : AddNewReminderComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        addNewReminderOutput(ComponentOutput.Common.BackPressed)
    }
}
