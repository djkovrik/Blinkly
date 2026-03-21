package com.sedsoftware.blinkly.component.garden.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.garden.GardenComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class GardenComponentDefault(
    private val componentContext: ComponentContext,
    private val gardenOutput: (ComponentOutput) -> Unit,
) : GardenComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        gardenOutput(ComponentOutput.Common.BackPressed)
    }
}
