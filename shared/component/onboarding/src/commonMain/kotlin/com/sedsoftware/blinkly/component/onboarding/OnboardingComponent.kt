package com.sedsoftware.blinkly.component.onboarding

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.sedsoftware.blinkly.component.step1.OnboardingStep1Component
import com.sedsoftware.blinkly.component.step2.OnboardingStep2Component
import com.sedsoftware.blinkly.component.step3.OnboardingStep3Component
import com.sedsoftware.blinkly.component.step4.OnboardingStep4Component
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component

interface OnboardingComponent : BackHandlerOwner {

    val childStack: Value<ChildStack<*, Child>>

    fun onBack()

    sealed class Child {
        data class Step1(val component: OnboardingStep1Component) : Child()
        data class Step2(val component: OnboardingStep2Component) : Child()
        data class Step3(val component: OnboardingStep3Component) : Child()
        data class Step4(val component: OnboardingStep4Component) : Child()
        data class Step5(val component: OnboardingStep5Component) : Child()
    }
}
