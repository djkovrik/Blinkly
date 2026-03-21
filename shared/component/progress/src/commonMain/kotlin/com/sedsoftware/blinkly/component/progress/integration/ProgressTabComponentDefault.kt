package com.sedsoftware.blinkly.component.progress.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class ProgressTabComponentDefault(
    private val componentContext: ComponentContext,
    private val progressTabOutput: (ComponentOutput) -> Unit,
) : ProgressTabComponent, ComponentContext by componentContext {

    override fun onAchievementsClick() {
        progressTabOutput(ComponentOutput.Progress.OpenAchievements)
    }

    override fun onGardenClick() {
        progressTabOutput(ComponentOutput.Progress.OpenGarden)
    }
}
