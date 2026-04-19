package com.sedsoftware.blinkly.component.step5.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.Intent
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.Label
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.State
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

internal interface InitialRemindersStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class OnInitialSetupSkip(val checked: Boolean) : Intent
        data class OnTimeSelectedFrom(val time: LocalTime) : Intent
        data class OnTimeSelectedUntil(val time: LocalTime) : Intent
        data class OnIntervalChanged(val interval: Int) : Intent
        data class OnWeekDayToggled(val weekDay: DayOfWeek) : Intent
        data object OnInitialSetupApply : Intent
        data object OnInitialSetupClear : Intent
    }

    data class State(
        val shouldSkipSetup: Boolean = false,
        val remindFrom: LocalTime = LocalTime(10, 0),
        val remindUntil: LocalTime = LocalTime(18, 0),
        val remindIntervalMinutes: Int = 20,
        val selectedDays: List<DayOfWeek> = DayOfWeek.entries.toList(),
        val createdReminderDays: List<DayOfWeek> = emptyList(),
        val createdReminderTimes: List<LocalTime> = emptyList(),
        val displayCreatedReminders: Boolean = false,
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
    }
}
