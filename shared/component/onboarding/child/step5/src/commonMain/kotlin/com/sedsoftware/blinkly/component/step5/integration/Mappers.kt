package com.sedsoftware.blinkly.component.step5.integration

import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component.Model
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component.ValidationError
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.ValidationError as StoreValidationError
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.State

internal val stateToModel: (State) -> Model = {
    Model(
        showInitialSetup = it.showInitialSetup,
        selectedTimeFrom = it.remindFrom,
        selectedTimeUntil = it.remindUntil,
        selectedInterval = it.remindIntervalMinutes,
        selectedDays = it.selectedDays,
        createdRemindersCount = it.createdReminders.size,
        initialSetupApplied = it.initialSetupApplied,
        isSaving = it.isSaving,
        validationError = it.validationError?.toComponentError(),
    )
}

private fun StoreValidationError.toComponentError(): ValidationError =
    when (this) {
        StoreValidationError.EMPTY_DAYS -> ValidationError.EMPTY_DAYS
        StoreValidationError.INVALID_PERIOD -> ValidationError.INVALID_PERIOD
        StoreValidationError.INVALID_INTERVAL -> ValidationError.INVALID_INTERVAL
    }
