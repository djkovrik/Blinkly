package com.sedsoftware.blinkly.component.step5.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component.Model
import com.sedsoftware.blinkly.component.step5.domain.InitialRemindersManager
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStoreProvider
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

class OnboardingStep5ComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val reminderManager: BlinklyReminderManager,
    private val onboardingOutput: (ComponentOutput) -> Unit,
) : OnboardingStep5Component, ComponentContext by componentContext {

    private val store: InitialRemindersStore =
        instanceKeeper.getStore {
            InitialRemindersStoreProvider(
                storeFactory = storeFactory,
                manager = InitialRemindersManager(reminderManager),
                mainContext = dispatchers.main,
                ioContext = dispatchers.io,
            ).create()
        }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onNextClick() {
        onboardingOutput(ComponentOutput.Onboarding.GoToHomeScreen)
    }

    override fun onBackClick() {
        onboardingOutput(ComponentOutput.Onboarding.GoBack)
    }

    override fun onSkipInitialSetupCheck(checked: Boolean) {
        store.accept(InitialRemindersStore.Intent.OnInitialSetupSkip(checked))
    }

    override fun onSelectTimeFrom(time: LocalTime) {
        store.accept(InitialRemindersStore.Intent.OnTimeSelectedFrom(time))
    }

    override fun onSelectTimeUntil(time: LocalTime) {
        store.accept(InitialRemindersStore.Intent.OnTimeSelectedUntil(time))
    }

    override fun onSelectInterval(interval: Int) {
        store.accept(InitialRemindersStore.Intent.OnIntervalChanged(interval))
    }

    override fun onToggleDay(weekDay: DayOfWeek) {
        store.accept(InitialRemindersStore.Intent.OnWeekDayToggled(weekDay))
    }

    override fun onCreateReminders() {
        store.accept(InitialRemindersStore.Intent.OnInitialSetupApply)
    }

    override fun onClearReminders() {
        store.accept(InitialRemindersStore.Intent.OnInitialSetupClear)
    }
}
