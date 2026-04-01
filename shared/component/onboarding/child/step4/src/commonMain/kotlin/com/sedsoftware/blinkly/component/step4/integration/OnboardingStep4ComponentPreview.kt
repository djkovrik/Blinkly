package com.sedsoftware.blinkly.component.step4.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.step4.OnboardingStep4Component
import com.sedsoftware.blinkly.component.step4.OnboardingStep4Component.Model

class OnboardingStep4ComponentPreview(
    checkboxSelected: Boolean,
) : OnboardingStep4Component {

    override val model: Value<Model> =
        MutableValue(
            Model(
                checkboxSelected = checkboxSelected,
            )
        )

    override fun onCheckboxSelect(checked: Boolean) = Unit
    override fun onNextClick() = Unit
    override fun onBackClick() = Unit
}
