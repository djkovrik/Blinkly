package com.sedsoftware.blinkly.component.step4.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.sedsoftware.blinkly.component.step4.OnboardingStep4Component
import com.sedsoftware.blinkly.component.step4.OnboardingStep4Component.Model
import com.sedsoftware.blinkly.component.step4.store.DisclaimerStore
import com.sedsoftware.blinkly.component.step4.store.DisclaimerStoreProvider
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.utils.asValue

class OnboardingStep4ComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val onboardingOutput: (ComponentOutput) -> Unit,
) : OnboardingStep4Component, ComponentContext by componentContext {

    private val store: DisclaimerStore =
        instanceKeeper.getStore {
            DisclaimerStoreProvider(
                storeFactory = storeFactory,
                mainContext = dispatchers.main,
            ).create()
        }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onCheckboxSelect(checked: Boolean) {
        store.accept(DisclaimerStore.Intent.OnCheckboxChecked(checked))
    }

    override fun onNextClick() {
        onboardingOutput(ComponentOutput.Onboarding.GoToStep5)
    }

    override fun onBackClick() {
        onboardingOutput(ComponentOutput.Onboarding.GoBack)
    }
}
