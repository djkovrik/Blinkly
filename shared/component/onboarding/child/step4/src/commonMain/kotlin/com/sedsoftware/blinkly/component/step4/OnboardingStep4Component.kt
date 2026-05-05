package com.sedsoftware.blinkly.component.step4

import com.arkivanov.decompose.value.Value

interface OnboardingStep4Component {

    val model: Value<Model>

    fun onCheckboxSelect(checked: Boolean)
    fun onNextClick()
    fun onBackClick()

    data class Model(
        val checkboxSelected: Boolean,
    )
}
