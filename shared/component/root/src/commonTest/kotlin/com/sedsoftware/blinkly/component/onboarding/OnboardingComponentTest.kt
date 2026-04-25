package com.sedsoftware.blinkly.component.onboarding

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.onboarding.integration.OnboardingComponentDefault
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderType
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class OnboardingComponentTest : ComponentTest<OnboardingComponent>() {

    private val testReminders: List<Reminder> =
        listOf(
            Reminder(
                uuid = "uuid",
                date = LocalDateTime(2026, 3, 8, 12, 34),
                type = ReminderType.TWENTY_X3,
                interval = ReminderInterval.DAILY,
                weekDays = DayOfWeek.entries.toList(),
            )
        )

    private val remindersFlow: MutableStateFlow<List<Reminder>> = MutableStateFlow(emptyList())

    private val reminderManagerMock: BlinklyReminderManager = mock {
        every { reminders } returns remindersFlow
        everySuspend { scheduleWeeklyDayPeriod(any(), any(), any(), any()) } returns Unit
        everySuspend { cancelAll() } returns Unit
    }

    @Test
    fun `when component created then onboarding first step is on top of the stack`() = runTest(testScheduler) {
        // given
        // when
        // then
        assertThat(component.childStack.active.instance is OnboardingComponent.Child.Step1).isTrue()
    }

    @Test
    fun `when onNextClick called for components then stack updated with new steps`() = runTest(testScheduler) {
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
    fun `when onBackClick called for components then stack updated with removed steps`() = runTest(testScheduler) {
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

    @Test
    fun `when onSkipInitialSetupCheck then last step state updated`() = runTest(testScheduler) {
        // given
        val step5 = getStep5Component()
        // when
        step5.onSkipInitialSetupCheck(true)
        testScheduler.advanceUntilIdle()
        // then
        assertThat(step5.model.value.skipInitialSetup).isTrue()
        // when
        step5.onSkipInitialSetupCheck(false)
        testScheduler.advanceUntilIdle()
        // then
        assertThat(step5.model.value.skipInitialSetup).isFalse()
    }

    @Test
    fun `when time params selected then last step state updated`() = runTest(testScheduler) {
        // given
        val step5 = getStep5Component()
        val timeFrom = LocalTime(1, 2)
        val timeUntil = LocalTime(3, 4)
        val interval = 5
        // when
        step5.onSelectTimeFrom(timeFrom)
        step5.onSelectTimeUntil(timeUntil)
        step5.onSelectInterval(interval)
        testScheduler.advanceUntilIdle()
        // then
        with(step5.model.value) {
            assertThat(selectedTimeFrom).isEqualTo(timeFrom)
            assertThat(selectedTimeUntil).isEqualTo(timeUntil)
            assertThat(selectedInterval).isEqualTo(interval)
        }
    }

    @Test
    fun `when day toggled then last step state updated`() = runTest(testScheduler) {
        // given
        val step5 = getStep5Component()
        val targetDay = DayOfWeek.WEDNESDAY
        assertThat(step5.model.value.selectedDays).contains(targetDay)
        // when
        step5.onToggleDay(targetDay)
        testScheduler.advanceUntilIdle()
        // then
        assertThat(step5.model.value.selectedDays).doesNotContain(targetDay)
        // when
        step5.onToggleDay(targetDay)
        testScheduler.advanceUntilIdle()
        // then
        assertThat(step5.model.value.selectedDays).contains(targetDay)
    }

    @Test
    fun `when created reminders changed then last step state updated`() = runTest(testScheduler) {
        // given
        val step5 = getStep5Component()
        with(step5.model.value) {
            assertThat(initialReminderDays).isEmpty()
            assertThat(initialReminderTimes).isEmpty()
            assertThat(initialRemindersVisible).isFalse()
        }

        // when
        step5.onCreateReminders()
        remindersFlow.emit(testReminders)
        testScheduler.advanceUntilIdle()
        // then
        with(step5.model.value) {
            assertThat(initialReminderDays).isNotEmpty()
            assertThat(initialReminderTimes).isNotEmpty()
            assertThat(initialRemindersVisible).isTrue()
        }

        // when
        step5.onClearReminders()
        remindersFlow.emit(emptyList())
        testScheduler.advanceUntilIdle()
        // then
        with(step5.model.value) {
            assertThat(initialReminderDays).isEmpty()
            assertThat(initialReminderTimes).isEmpty()
            assertThat(initialRemindersVisible).isFalse()
        }
    }

    private fun getStep5Component(): OnboardingStep5Component {
        (component.childStack.active.instance as? OnboardingComponent.Child.Step1)?.component?.onNextClick()
        (component.childStack.active.instance as? OnboardingComponent.Child.Step2)?.component?.onNextClick()
        (component.childStack.active.instance as? OnboardingComponent.Child.Step3)?.component?.onNextClick()
        (component.childStack.active.instance as? OnboardingComponent.Child.Step4)?.component?.onNextClick()
        return (component.childStack.active.instance as? OnboardingComponent.Child.Step5)?.component!!
    }

    override fun createComponent(): OnboardingComponent =
        OnboardingComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            reminderManager = reminderManagerMock,
            dispatchers = testDispatchers,
            onboardingOutput = { componentOutput.add(it) },
        )
}
