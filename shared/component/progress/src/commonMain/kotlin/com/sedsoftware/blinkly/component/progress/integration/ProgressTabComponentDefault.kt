package com.sedsoftware.blinkly.component.progress.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class ProgressTabComponentDefault(
    componentContext: ComponentContext,
    progressTabOutput: (ComponentOutput) -> Unit,
) : ProgressTabComponent, ComponentContext by componentContext
