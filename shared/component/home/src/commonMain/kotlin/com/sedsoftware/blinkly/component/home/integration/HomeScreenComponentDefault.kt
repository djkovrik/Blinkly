package com.sedsoftware.blinkly.component.home.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class HomeScreenComponentDefault(
    private val componentContext: ComponentContext,
    private val dispatchers: BlinklyDispatchers,
    private val settings: BlinklySettings,
    private val homeScreenOutput: (ComponentOutput) -> Unit,
) : HomeScreenComponent, ComponentContext by componentContext {

    private val scope: CoroutineScope = CoroutineScope(dispatchers.io)

    init {
        lifecycle.doOnDestroy {
            scope.cancel()
        }

        markOnboardingCompleted()
    }

    private fun markOnboardingCompleted() {
        scope.launch {
            if (!settings.onboardingDisplayed) {
                settings.onboardingDisplayed = true
            }
        }
    }
}
