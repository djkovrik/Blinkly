package com.sedsoftware.blinkly.component.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent

interface RootComponent : BackHandlerOwner {

    val childStack: Value<ChildStack<*, Child>>

    fun onBack()

    sealed class Child {
        data class Onboarding(val component: OnboardingComponent) : Child()
        data class HomeScreen(val component: HomeScreenComponent) : Child()
    }
}
