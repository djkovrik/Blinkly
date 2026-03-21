package com.sedsoftware.blinkly.component.achievements.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class AchievementsComponentDefault(
    private val componentContext: ComponentContext,
    private val achievementsOutput: (ComponentOutput) -> Unit,
) : AchievementsComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        achievementsOutput(ComponentOutput.Common.BackPressed)
    }
}
