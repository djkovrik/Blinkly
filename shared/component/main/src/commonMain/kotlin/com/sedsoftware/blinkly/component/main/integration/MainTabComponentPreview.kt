package com.sedsoftware.blinkly.component.main.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.main.MainTabComponent
import com.sedsoftware.blinkly.component.main.MainTabComponent.Model

class MainTabComponentPreview(
    model: Model = Model.EMPTY,
) : MainTabComponent {

    override val model: Value<Model> = MutableValue(model)

    override fun onPreferencesClick() = Unit
    override fun onTreeClick() = Unit
    override fun onPrimaryCtaClick() = Unit
}
