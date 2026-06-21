package com.sedsoftware.blinkly.component.newreminder.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.newreminder.domain.AddNewReminderManager
import com.sedsoftware.blinkly.component.newreminder.domain.model.ReminderScheduleType
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore.Intent
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore.Label
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore.State
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore.ValidationError
import com.sedsoftware.blinkly.utils.StoreProvider
import com.sedsoftware.blinkly.utils.unwrap
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.coroutines.CoroutineContext

internal class AddNewReminderStoreProvider(
    private val storeFactory: StoreFactory,
    private val manager: AddNewReminderManager,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
) {
    @StoreProvider
    fun create(autoInit: Boolean = true): AddNewReminderStore =
        object : AddNewReminderStore, Store<Intent, State, Label> by storeFactory.create<Intent, Nothing, Msg, State, Label>(
            name = "AddNewReminderStore",
            initialState = State(),
            autoInit = autoInit,
            executorFactory = coroutineExecutorFactory(mainContext) {
                onIntent<Intent.ScheduleTypeSelected> {
                    dispatch(Msg.ScheduleTypeChanged(it.type))
                }

                onIntent<Intent.DailyTimeSelected> {
                    dispatch(Msg.DailyTimeChanged(it.time))
                }

                onIntent<Intent.WeeklyTimeSelected> {
                    dispatch(Msg.WeeklyTimeChanged(it.time))
                }

                onIntent<Intent.WeeklyDaySelected> {
                    dispatch(Msg.WeeklyDayChanged(it.dayOfWeek))
                }

                onIntent<Intent.PeriodTimeFromSelected> {
                    dispatch(Msg.PeriodTimeFromChanged(it.time))
                }

                onIntent<Intent.PeriodTimeUntilSelected> {
                    dispatch(Msg.PeriodTimeUntilChanged(it.time))
                }

                onIntent<Intent.PeriodIntervalSelected> {
                    dispatch(Msg.PeriodIntervalChanged(it.interval))
                }

                onIntent<Intent.PeriodDayToggled> {
                    dispatch(Msg.PeriodDayToggled(it.dayOfWeek))
                }

                onIntent<Intent.CreateClicked> {
                    if (state().isSaving) return@onIntent

                    val validationError = state().validationError()

                    if (validationError != null) {
                        dispatch(Msg.ValidationFailed(validationError))
                    } else {
                        val state = state()
                        dispatch(Msg.SavingChanged(true))

                        launch {
                            unwrap(
                                result = withContext(ioContext) {
                                    when (state.scheduleType) {
                                        ReminderScheduleType.DAILY -> manager.scheduleDaily(state.dailyTime)
                                        ReminderScheduleType.WEEKLY_SINGLE -> manager.scheduleWeeklySingle(
                                            time = state.weeklyTime,
                                            dayOfWeek = state.weeklyDay,
                                        )

                                        ReminderScheduleType.WEEKLY_DAY_PERIOD -> manager.scheduleWeeklyDayPeriod(
                                            from = state.periodTimeFrom,
                                            until = state.periodTimeUntil,
                                            intervalMinutes = state.periodInterval,
                                            days = state.periodDays,
                                        )
                                    }
                                },
                                onSuccess = {
                                    dispatch(Msg.SavingChanged(false))
                                    publish(Label.ReminderCreated)
                                },
                                onError = { throwable ->
                                    dispatch(Msg.SavingChanged(false))
                                    publish(Label.ErrorCaught(throwable))
                                },
                            )
                        }
                    }
                }

                onIntent<Intent.ValidationMessageShown> {
                    dispatch(Msg.ValidationMessageShown)
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.ScheduleTypeChanged -> copy(
                        scheduleType = msg.type,
                        validationError = null,
                    )

                    is Msg.DailyTimeChanged -> copy(
                        dailyTime = msg.time,
                        validationError = null,
                    )

                    is Msg.WeeklyTimeChanged -> copy(
                        weeklyTime = msg.time,
                        validationError = null,
                    )

                    is Msg.WeeklyDayChanged -> copy(
                        weeklyDay = msg.dayOfWeek,
                        validationError = null,
                    )

                    is Msg.PeriodTimeFromChanged -> copy(
                        periodTimeFrom = msg.time,
                        validationError = null,
                    )

                    is Msg.PeriodTimeUntilChanged -> copy(
                        periodTimeUntil = msg.time,
                        validationError = null,
                    )

                    is Msg.PeriodIntervalChanged -> copy(
                        periodInterval = msg.interval,
                        validationError = null,
                    )

                    is Msg.PeriodDayToggled -> copy(
                        periodDays = if (periodDays.contains(msg.dayOfWeek)) {
                            periodDays - msg.dayOfWeek
                        } else {
                            periodDays + msg.dayOfWeek
                        },
                        validationError = null,
                    )

                    is Msg.ValidationFailed -> copy(
                        validationError = msg.error,
                    )

                    is Msg.SavingChanged -> copy(
                        isSaving = msg.saving,
                    )

                    is Msg.ValidationMessageShown -> copy(
                        validationError = null,
                    )
                }
            },
        ) {}

    sealed interface Msg {
        data class ScheduleTypeChanged(val type: ReminderScheduleType) : Msg
        data class DailyTimeChanged(val time: LocalTime) : Msg
        data class WeeklyTimeChanged(val time: LocalTime) : Msg
        data class WeeklyDayChanged(val dayOfWeek: DayOfWeek) : Msg
        data class PeriodTimeFromChanged(val time: LocalTime) : Msg
        data class PeriodTimeUntilChanged(val time: LocalTime) : Msg
        data class PeriodIntervalChanged(val interval: Int) : Msg
        data class PeriodDayToggled(val dayOfWeek: DayOfWeek) : Msg
        data class ValidationFailed(val error: ValidationError) : Msg
        data class SavingChanged(val saving: Boolean) : Msg
        data object ValidationMessageShown : Msg
    }

    private fun State.validationError(): ValidationError? =
        when {
            scheduleType != ReminderScheduleType.WEEKLY_DAY_PERIOD -> null
            periodDays.isEmpty() -> ValidationError.EMPTY_DAYS
            periodTimeFrom >= periodTimeUntil -> ValidationError.INVALID_PERIOD
            periodInterval <= 0 -> ValidationError.INVALID_INTERVAL
            else -> null
        }
}
