package com.sedsoftware.blinkly.component.preferences.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.preferences.PreferencesComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class PreferencesComponentDefault(
    private val componentContext: ComponentContext,
    private val preferencesOutput: (ComponentOutput) -> Unit,
) : PreferencesComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        preferencesOutput(ComponentOutput.Common.BackPressed)
    }
}
