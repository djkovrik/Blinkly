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
        val remindFrom: LocalTime = LocalTime(0, 0),
        val remindUntil: LocalTime = LocalTime(1, 0),
        val remindIntervalMinutes: Int = 1,
        val selectedDays: List<DayOfWeek> = DayOfWeek.entries.toList(),
        val createdReminders: List<Reminder> = emptyList(),
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
    }
}
