package com.sedsoftware.blinkly.component.reminders.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class RemindersTabComponentDefault(
    componentContext: ComponentContext,
    remindersTabOutput: (ComponentOutput) -> Unit,
) : RemindersTabComponent, ComponentContext by componentContext
