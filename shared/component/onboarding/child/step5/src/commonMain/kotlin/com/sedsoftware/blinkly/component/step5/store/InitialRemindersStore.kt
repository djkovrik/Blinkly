package com.sedsoftware.blinkly.component.step5.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.Intent
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.Label
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.State
import com.sedsoftware.blinkly.domain.model.Reminder
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

internal interface InitialRemindersStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class OnInitialSetupChoice(val show: Boolean) : Intent
        data class OnTimeSelectedFrom(val time: LocalTime) : Intent
        data class OnTimeSelectedUntil(val time: LocalTime) : Intent
        data class OnIntervalChanged(val interval: Int) : Intent
        data class OnWeekDayToggled(val weekDay: DayOfWeek) : Intent
        data object OnInitialSetupApply : Intent
        data object OnInitialSetupClear : Intent
    }

    data class State(
        val showInitialSetup: Boolean = false,
        val permissionChecked: Boolean = false,
        val permissionGranted: Boolean = false,
        val remindFrom: LocalTime = LocalTime(DEFAULT_HOUR_START, 0),
        val remindUntil: LocalTime = LocalTime(DEFAULT_HOUR_END, 0),
        val remindIntervalMinutes: Int = DEFAULT_REMINDER_INTERVAL,
        val selectedDays: List<DayOfWeek> = DayOfWeek.entries.toList(),
        val createdReminders: List<Reminder> = emptyList(),
        val initialSetupApplied: Boolean = false,
        val isSaving: Boolean = false,
        val validationError: ValidationError? = null,
    )

    enum class ValidationError {
        EMPTY_DAYS,
        INVALID_PERIOD,
        INVALID_INTERVAL,
    }

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
    }

    private companion object {
        const val DEFAULT_HOUR_START = 10
        const val DEFAULT_HOUR_END = 18
        const val DEFAULT_REMINDER_INTERVAL = 20
    }
}
