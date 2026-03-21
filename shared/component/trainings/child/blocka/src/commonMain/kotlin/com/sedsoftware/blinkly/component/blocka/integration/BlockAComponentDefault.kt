package com.sedsoftware.blinkly.component.blocka.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.blocka.BlockAComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class BlockAComponentDefault(
    private val componentContext: ComponentContext,
    private val blockAOutput: (ComponentOutput) -> Unit,
) : BlockAComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        blockAOutput(ComponentOutput.Common.BackPressed)
    }
}
