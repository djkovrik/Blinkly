package com.sedsoftware.blinkly.component.newreminder.integration

import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.Model
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.ScheduleType
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.ValidationError
import com.sedsoftware.blinkly.component.newreminder.domain.model.ReminderScheduleType
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore.ValidationError as StoreValidationError
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore.State

internal val stateToModel: (State) -> Model = {
    Model(
        scheduleType = it.scheduleType.toComponentType(),
        dailyTime = it.dailyTime,
        weeklyTime = it.weeklyTime,
        weeklyDay = it.weeklyDay,
        periodTimeFrom = it.periodTimeFrom,
        periodTimeUntil = it.periodTimeUntil,
        periodInterval = it.periodInterval,
        periodDays = it.periodDays,
        isSaving = it.isSaving,
        validationError = it.validationError?.toComponentError(),
    )
}

internal fun ScheduleType.toStoreType(): ReminderScheduleType =
    when (this) {
        ScheduleType.DAILY -> ReminderScheduleType.DAILY
        ScheduleType.WEEKLY_SINGLE -> ReminderScheduleType.WEEKLY_SINGLE
        ScheduleType.WEEKLY_DAY_PERIOD -> ReminderScheduleType.WEEKLY_DAY_PERIOD
    }

private fun ReminderScheduleType.toComponentType(): ScheduleType =
    when (this) {
        ReminderScheduleType.DAILY -> ScheduleType.DAILY
        ReminderScheduleType.WEEKLY_SINGLE -> ScheduleType.WEEKLY_SINGLE
        ReminderScheduleType.WEEKLY_DAY_PERIOD -> ScheduleType.WEEKLY_DAY_PERIOD
    }

private fun StoreValidationError.toComponentError(): ValidationError =
    when (this) {
        StoreValidationError.EMPTY_DAYS -> ValidationError.EMPTY_DAYS
        StoreValidationError.INVALID_PERIOD -> ValidationError.INVALID_PERIOD
        StoreValidationError.INVALID_INTERVAL -> ValidationError.INVALID_INTERVAL
    }
