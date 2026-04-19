package com.sedsoftware.blinkly.component.onboarding

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.onboarding.integration.OnboardingComponentDefault
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class OnboardingComponentTest : ComponentTest<OnboardingComponent>() {

    @Test
    fun `when component created then onboarding first step is on top of the stack`() = runTest(testScheduler) {
        // given
        // when
        // then
        assertThat(component.childStack.active.instance is OnboardingComponent.Child.Step1).isTrue()
    }

    @Test
    fun `when nextStep called for components then stack updated with new steps`() = runTest(testScheduler) {
        // given
        val childStep1 = component.childStack.active.instance as? OnboardingComponent.Child.Step1
        assertThat(childStep1).isNotNull()
        // when
        childStep1?.component?.onNextClick()
        // then
        val childStep2 = component.childStack.active.instance as? OnboardingComponent.Child.Step2
        assertThat(childStep2).isNotNull()
        assertThat(component.childStack.items.size).isEqualTo(2)
        // when
        childStep2?.component?.onNextClick()
        // then
        val childStep3 = component.childStack.active.instance as? OnboardingComponent.Child.Step3
        assertThat(childStep3).isNotNull()
        assertThat(component.childStack.items.size).isEqualTo(3)
        // when
        childStep3?.component?.onNextClick()
        // then
        val childStep4 = component.childStack.active.instance as? OnboardingComponent.Child.Step4
        assertThat(childStep4).isNotNull()
        assertThat(component.childStack.items.size).isEqualTo(4)
        // when
        childStep4?.component?.onNextClick()
        // then
        val childStep5 = component.childStack.active.instance as? OnboardingComponent.Child.Step5
        assertThat(childStep5).isNotNull()
        assertThat(component.childStack.items.size).isEqualTo(5)
        // when
        childStep5?.component?.onNextClick()
        // then
        assertThat(componentOutput).contains(ComponentOutput.Onboarding.GoToHomeScreen)
    }

    @Test
    fun `when previousStep called for components then stack updated with removed steps`() = runTest(testScheduler) {
        // given
        var childStep1 = component.childStack.active.instance as? OnboardingComponent.Child.Step1
        childStep1?.component?.onNextClick()
        var childStep2 = component.childStack.active.instance as? OnboardingComponent.Child.Step2
        childStep2?.component?.onNextClick()
        var childStep3 = component.childStack.active.instance as? OnboardingComponent.Child.Step3
        childStep3?.component?.onNextClick()
        var childStep4 = component.childStack.active.instance as? OnboardingComponent.Child.Step4
        childStep4?.component?.onNextClick()
        val childStep5 = component.childStack.active.instance as? OnboardingComponent.Child.Step5
        assertThat(childStep5).isNotNull()
        // when
        childStep5?.component?.onBackClick()
        childStep4 = component.childStack.active.instance as? OnboardingComponent.Child.Step4
        // then
        assertThat(childStep4).isNotNull()
        // when
        childStep4?.component?.onBackClick()
        childStep3 = component.childStack.active.instance as? OnboardingComponent.Child.Step3
        // then
        assertThat(childStep3).isNotNull()
        // when
        childStep3?.component?.onBackClick()
        childStep2 = component.childStack.active.instance as? OnboardingComponent.Child.Step2
        // then
        assertThat(childStep2).isNotNull()
        // when
        childStep2?.component?.onBackClick()
        childStep1 = component.childStack.active.instance as? OnboardingComponent.Child.Step1
        // then
        assertThat(childStep1).isNotNull()
    }

    @Test
    fun `when onBack called for components then stack updated with removed steps`() = runTest(testScheduler) {
        // given
        var childStep1 = component.childStack.active.instance as? OnboardingComponent.Child.Step1
        childStep1?.component?.onNextClick()
        val childStep2 = component.childStack.active.instance as? OnboardingComponent.Child.Step2
        assertThat(childStep2).isNotNull()
        // when
        component.onBack()
        childStep1 = component.childStack.active.instance as? OnboardingComponent.Child.Step1
        assertThat(childStep1).isNotNull()
    }

    @Test
    fun `when onCheckboxSelect called then component state should be updated`() = runTest(testScheduler) {
        // given
        val childStep1 = component.childStack.active.instance as? OnboardingComponent.Child.Step1
        childStep1?.component?.onNextClick()
        val childStep2 = component.childStack.active.instance as? OnboardingComponent.Child.Step2
        childStep2?.component?.onNextClick()
        val childStep3 = component.childStack.active.instance as? OnboardingComponent.Child.Step3
        childStep3?.component?.onNextClick()
        val childStep4 = component.childStack.active.instance as? OnboardingComponent.Child.Step4
        assertThat(childStep4?.component?.model?.value?.checkboxSelected).isEqualTo(false)
        // when
        childStep4?.component?.onCheckboxSelect(true)
        // then
        assertThat(childStep4?.component?.model?.value?.checkboxSelected).isEqualTo(true)
        // when
        childStep4?.component?.onCheckboxSelect(false)
        // then
        assertThat(childStep4?.component?.model?.value?.checkboxSelected).isEqualTo(false)
    }

    override fun createComponent(): OnboardingComponent =
        OnboardingComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            onboardingOutput = { componentOutput.add(it) },
        )
}
