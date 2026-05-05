package com.sedsoftware.blinkly.component.step5.integration

import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component.Model
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.State

internal val stateToModel: (State) -> Model = {
    Model(
        showInitialSetup = it.showInitialSetup,
        selectedTimeFrom = it.remindFrom,
        selectedTimeUntil = it.remindUntil,
        selectedInterval = it.remindIntervalMinutes,
        selectedDays = it.selectedDays,
        createdRemindersCount = it.createdReminders.size,
    )
}
