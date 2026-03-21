package com.sedsoftware.blinkly.component.reminders.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class RemindersTabComponentDefault(
    private val componentContext: ComponentContext,
    private val remindersTabOutput: (ComponentOutput) -> Unit,
) : RemindersTabComponent, ComponentContext by componentContext {

    override fun onAddNewClick() {
        remindersTabOutput(ComponentOutput.Reminders.OpenAddNew)
    }
}
