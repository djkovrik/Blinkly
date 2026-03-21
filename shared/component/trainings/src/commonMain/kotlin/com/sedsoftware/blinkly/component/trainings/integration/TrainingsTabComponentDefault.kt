package com.sedsoftware.blinkly.component.trainings.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class TrainingsTabComponentDefault(
    componentContext: ComponentContext,
    trainingsTabOutput: (ComponentOutput) -> Unit,
) : TrainingsTabComponent, ComponentContext by componentContext
