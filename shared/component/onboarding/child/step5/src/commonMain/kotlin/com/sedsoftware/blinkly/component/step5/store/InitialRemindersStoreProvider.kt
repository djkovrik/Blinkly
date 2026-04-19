package com.sedsoftware.blinkly.component.step5.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.step5.domain.InitialRemindersManager
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.Intent
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.Label
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.State
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.utils.StoreProvider
import com.sedsoftware.blinkly.utils.unwrap
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.coroutines.CoroutineContext

internal class InitialRemindersStoreProvider(
    private val storeFactory: StoreFactory,
    private val manager: InitialRemindersManager,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
) {
    @StoreProvider
    fun create(autoInit: Boolean = true): InitialRemindersStore =
        object : InitialRemindersStore, Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
            name = "InitialRemindersStore",
            initialState = State(),
            autoInit = autoInit,
            bootstrapper = coroutineBootstrapper(mainContext) {
                dispatch(Action.ObserveCreatedReminders)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                onAction<Action.ObserveCreatedReminders> {
                    launch {
                        manager.observeReminders()
                            .catch { publish(Label.ErrorCaught(it)) }
                            .collect { dispatch(Msg.RemindersRefreshed(it)) }
                    }
                }

                onIntent<Intent.OnInitialSetupSkip> {
                    dispatch(Msg.SkipSelectionChanged(it.checked))
                }

                onIntent<Intent.OnTimeSelectedFrom> {
                    dispatch(Msg.TimeSelectedFrom(it.time))
                }

                onIntent<Intent.OnTimeSelectedUntil> {
                    dispatch(Msg.TimeSelectedTo(it.time))
                }

                onIntent<Intent.OnIntervalChanged> {
                    dispatch(Msg.IntervalChanged(it.interval))
                }

                onIntent<Intent.OnWeekDayToggled> {
                    dispatch(Msg.WeekDayToggled(it.weekDay))
                }

                onIntent<Intent.OnInitialSetupApply> {
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.setupInitial(state()) },
                            onSuccess = {
                                dispatch(Msg.RemindersCreated)
                            },
                            onError = { throwable ->
                                publish(Label.ErrorCaught(throwable))
                            }
                        )
                    }
                }

                onIntent<Intent.OnInitialSetupClear> {
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.clearInitial() },
                            onSuccess = {
                                dispatch(Msg.RemindersDeleted)
                            },
                            onError = { throwable ->
                                publish(Label.ErrorCaught(throwable))
                            }
                        )
                    }
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.RemindersRefreshed -> copy(
                        createdReminderDays = if (msg.items.isNotEmpty()) {
                            msg.items[0].weekDays
                        } else {
                            emptyList()
                        },
                        createdReminderTimes = if (msg.items.isNotEmpty()) {
                            msg.items.map { it.date.time }
                        } else {
                            emptyList()
                        },
                    )

                    is Msg.SkipSelectionChanged -> copy(
                        shouldSkipSetup = msg.checked,
                    )

                    is Msg.TimeSelectedFrom -> copy(
                        remindFrom = msg.time,
                    )

                    is Msg.TimeSelectedTo -> copy(
                        remindUntil = msg.time,
                    )

                    is Msg.IntervalChanged -> copy(
                        remindIntervalMinutes = msg.interval,
                    )

                    is Msg.WeekDayToggled -> copy(
                        selectedDays = if (selectedDays.contains(msg.weekDay)) {
                            selectedDays - msg.weekDay
                        } else {
                            selectedDays + msg.weekDay
                        }
                    )

                    is Msg.RemindersCreated -> copy(
                        displayCreatedReminders = true,
                    )

                    is Msg.RemindersDeleted -> copy(
                        displayCreatedReminders = false,
                    )
                }
            }
        ) {}

    sealed interface Action {
        data object ObserveCreatedReminders : Action
    }

    sealed interface Msg {
        data class RemindersRefreshed(val items: List<Reminder>) : Msg
        data class SkipSelectionChanged(val checked: Boolean) : Msg
        data class TimeSelectedFrom(val time: LocalTime) : Msg
        data class TimeSelectedTo(val time: LocalTime) : Msg
        data class IntervalChanged(val interval: Int) : Msg
        data class WeekDayToggled(val weekDay: DayOfWeek) : Msg
        data object RemindersCreated : Msg
        data object RemindersDeleted : Msg
    }
}
