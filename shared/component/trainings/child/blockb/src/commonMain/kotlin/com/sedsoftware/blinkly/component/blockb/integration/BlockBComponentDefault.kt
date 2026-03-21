package com.sedsoftware.blinkly.component.blockb.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.blockb.BlockBComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class BlockBComponentDefault(
    private val componentContext: ComponentContext,
    private val blockBOutput: (ComponentOutput) -> Unit,
) : BlockBComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        blockBOutput(ComponentOutput.Common.BackPressed)
    }
}
