package com.sedsoftware.blinkly.component.main.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.main.MainTabComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class MainTabComponentDefault(
    componentContext: ComponentContext,
    mainTabOutput: (ComponentOutput) -> Unit,
) : MainTabComponent, ComponentContext by componentContext
