package com.sedsoftware.blinkly.component.blockc.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.blockc.BlockCComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class BlockCComponentDefault(
    private val componentContext: ComponentContext,
    private val blockCOutput: (ComponentOutput) -> Unit,
) : BlockCComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        blockCOutput(ComponentOutput.Common.BackPressed)
    }
}
