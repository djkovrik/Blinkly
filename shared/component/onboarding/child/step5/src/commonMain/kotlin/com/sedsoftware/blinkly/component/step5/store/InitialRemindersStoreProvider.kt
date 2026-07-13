package com.sedsoftware.blinkly.component.step5.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.step5.domain.InitialRemindersManager
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.Intent
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.Label
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.State
import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore.ValidationError
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.asBlinklyError
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
                dispatch(Action.CheckNotificationsPermission)
                dispatch(Action.ObserveGrantedPermission)
                dispatch(Action.ObserveCreatedReminders)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                onAction<Action.CheckNotificationsPermission> {
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.isNotificationsPermissionGranted() },
                            onSuccess = { granted ->
                                dispatch(Msg.NotificationPermissionChecked)
                                dispatch(Msg.NotificationPermissionChanged(granted))
                            },
                            onError = { throwable ->
                                publish(Label.ErrorCaught(BlinklyError.NotificationPermissionChecking(throwable)))
                            }
                        )
                    }
                }

                onAction<Action.ObserveGrantedPermission> {
                    launch {
                        manager.observePermissionEvents()
                            .catch { publish(Label.ErrorCaught(it.asBlinklyError(BlinklyError::NotificationPermissionChecking))) }
                            .collect { result ->
                                val granted = result == PermissionResult.Granted
                                dispatch(Msg.NotificationPermissionChanged(granted))
                                dispatch(Msg.ShowInitialSetupChanged(granted))

                                if (result == PermissionResult.DeniedAlways) {
                                    publish(Label.ErrorCaught(BlinklyError.NotificationPermissionDeniedAlways()))
                                }
                            }
                    }
                }

                onAction<Action.ObserveCreatedReminders> {
                    launch {
                        manager.observeReminders()
                            .catch { publish(Label.ErrorCaught(it.asBlinklyError(BlinklyError::InitialRemindersLoading))) }
                            .collect { dispatch(Msg.RemindersUpdated(it)) }
                    }
                }

                onIntent<Intent.OnInitialSetupChoice> {
                    if (state().isSaving) return@onIntent

                    if (it.show && state().permissionChecked && !state().permissionGranted) {
                        launch {
                            unwrap(
                                result = manager.requestNotificationsPermission(),
                                onSuccess = {},
                                onError = { throwable ->
                                    publish(Label.ErrorCaught(BlinklyError.NotificationPermissionRequesting(throwable)))
                                }
                            )
                        }
                    } else {
                        dispatch(Msg.ShowInitialSetupChanged(it.show))
                    }
                }

                onIntent<Intent.OnTimeSelectedFrom> {
                    if (state().isSaving) return@onIntent

                    dispatch(Msg.TimeSelectedFrom(it.time))
                }

                onIntent<Intent.OnTimeSelectedUntil> {
                    if (state().isSaving) return@onIntent

                    dispatch(Msg.TimeSelectedTo(it.time))
                }

                onIntent<Intent.OnIntervalChanged> {
                    if (state().isSaving) return@onIntent

                    dispatch(Msg.IntervalChanged(it.interval))
                }

                onIntent<Intent.OnWeekDayToggled> {
                    if (state().isSaving) return@onIntent

                    dispatch(Msg.WeekDayToggled(it.weekDay))
                }

                onIntent<Intent.OnInitialSetupApply> {
                    if (state().isSaving || !state().showInitialSetup) return@onIntent

                    val validationError = state().validationError()

                    if (validationError != null) {
                        dispatch(Msg.ValidationFailed(validationError))
                        return@onIntent
                    }

                    val setupState = state()
                    dispatch(Msg.SavingChanged(true))

                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.setupInitial(setupState) },
                            onSuccess = {
                                dispatch(Msg.InitialSetupApplied)
                            },
                            onError = { throwable ->
                                dispatch(Msg.SavingChanged(false))
                                publish(Label.ErrorCaught(BlinklyError.InitialRemindersCreating(throwable)))
                            }
                        )
                    }
                }

                onIntent<Intent.OnInitialSetupClear> {
                    if (state().isSaving) return@onIntent

                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.clearInitial() },
                            onSuccess = {
                                dispatch(Msg.RemindersDeleted)
                            },
                            onError = { throwable ->
                                publish(Label.ErrorCaught(BlinklyError.InitialRemindersClearing(throwable)))
                            }
                        )
                    }
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.NotificationPermissionChecked -> copy(
                        permissionChecked = true,
                    )

                    is Msg.NotificationPermissionChanged -> copy(
                        permissionGranted = msg.granted,
                    )

                    is Msg.RemindersUpdated -> copy(
                        createdReminders = msg.items,
                    )

                    is Msg.ShowInitialSetupChanged -> copy(
                        showInitialSetup = msg.checked,
                        initialSetupApplied = if (msg.checked) initialSetupApplied else false,
                        validationError = null,
                    )

                    is Msg.TimeSelectedFrom -> copy(
                        remindFrom = msg.time,
                        initialSetupApplied = false,
                        validationError = null,
                    )

                    is Msg.TimeSelectedTo -> copy(
                        remindUntil = msg.time,
                        initialSetupApplied = false,
                        validationError = null,
                    )

                    is Msg.IntervalChanged -> copy(
                        remindIntervalMinutes = msg.interval,
                        initialSetupApplied = false,
                        validationError = null,
                    )

                    is Msg.WeekDayToggled -> copy(
                        selectedDays = if (selectedDays.contains(msg.weekDay)) {
                            selectedDays - msg.weekDay
                        } else {
                            selectedDays + msg.weekDay
                        },
                        initialSetupApplied = false,
                        validationError = null,
                    )

                    is Msg.RemindersDeleted -> copy(
                        createdReminders = emptyList(),
                        initialSetupApplied = false,
                    )

                    is Msg.ValidationFailed -> copy(
                        validationError = msg.error,
                    )

                    is Msg.SavingChanged -> copy(
                        isSaving = msg.saving,
                    )

                    is Msg.InitialSetupApplied -> copy(
                        isSaving = false,
                        initialSetupApplied = true,
                        validationError = null,
                    )
                }
            }
        ) {}

    sealed interface Action {
        data object CheckNotificationsPermission : Action
        data object ObserveGrantedPermission : Action
        data object ObserveCreatedReminders : Action
    }

    sealed interface Msg {
        data object NotificationPermissionChecked : Msg
        data class NotificationPermissionChanged(val granted: Boolean) : Msg
        data class RemindersUpdated(val items: List<Reminder>) : Msg
        data class ShowInitialSetupChanged(val checked: Boolean) : Msg
        data class TimeSelectedFrom(val time: LocalTime) : Msg
        data class TimeSelectedTo(val time: LocalTime) : Msg
        data class IntervalChanged(val interval: Int) : Msg
        data class WeekDayToggled(val weekDay: DayOfWeek) : Msg
        data object RemindersDeleted : Msg
        data class ValidationFailed(val error: ValidationError) : Msg
        data class SavingChanged(val saving: Boolean) : Msg
        data object InitialSetupApplied : Msg
    }

    private fun State.validationError(): ValidationError? =
        when {
            selectedDays.isEmpty() -> ValidationError.EMPTY_DAYS
            remindFrom >= remindUntil -> ValidationError.INVALID_PERIOD
            remindIntervalMinutes <= 0 -> ValidationError.INVALID_INTERVAL
            else -> null
        }
}
