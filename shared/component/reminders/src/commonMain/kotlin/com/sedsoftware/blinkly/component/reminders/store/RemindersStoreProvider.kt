package com.sedsoftware.blinkly.component.reminders.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.reminders.domain.RemindersManager
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.Intent
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.Label
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.State
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.asBlinklyError
import com.sedsoftware.blinkly.utils.StoreProvider
import com.sedsoftware.blinkly.utils.unwrap
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class RemindersStoreProvider(
    private val storeFactory: StoreFactory,
    private val manager: RemindersManager,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
) {
    @StoreProvider
    fun create(autoInit: Boolean = true): RemindersStore =
        object : RemindersStore, Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
            name = "RemindersStore",
            initialState = State(),
            autoInit = autoInit,
            bootstrapper = coroutineBootstrapper(mainContext) {
                dispatch(Action.ObserveReminders)
                dispatch(Action.ObserveNotificationPermission)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                onAction<Action.ObserveReminders> {
                    launch {
                        manager.observeReminders()
                            .catch { publish(Label.ErrorCaught(it.asBlinklyError(BlinklyError::RemindersLoading))) }
                            .collect { dispatch(Msg.RemindersUpdated(it)) }
                    }
                }

                onAction<Action.ObserveNotificationPermission> {
                    launch {
                        manager.observePermissionEvents()
                            .catch {
                                dispatch(Msg.NotificationPermissionRequestFinished)
                                publish(Label.ErrorCaught(it.asBlinklyError(BlinklyError::NotificationPermissionChecking)))
                            }
                            .collect { result ->
                                if (!state().isRequestingNotificationPermission) return@collect

                                dispatch(Msg.NotificationPermissionRequestFinished)
                                when (result) {
                                    PermissionResult.Granted -> publish(Label.OpenAddNewReminder)
                                    PermissionResult.Denied -> Unit
                                    PermissionResult.DeniedAlways -> {
                                        publish(Label.ErrorCaught(BlinklyError.NotificationPermissionDeniedAlways()))
                                    }
                                }
                            }
                    }
                }

                onIntent<Intent.AddNewReminder> {
                    if (state().isRequestingNotificationPermission) return@onIntent

                    dispatch(Msg.NotificationPermissionRequestStarted)
                    launch {
                        val permissionGranted = withContext(ioContext) {
                            manager.isNotificationPermissionGranted()
                        }.getOrElse { throwable ->
                            dispatch(Msg.NotificationPermissionRequestFinished)
                            publish(Label.ErrorCaught(BlinklyError.NotificationPermissionChecking(throwable)))
                            return@launch
                        }

                        if (permissionGranted) {
                            dispatch(Msg.NotificationPermissionRequestFinished)
                            publish(Label.OpenAddNewReminder)
                        } else {
                            unwrap(
                                result = manager.requestNotificationPermission(),
                                onSuccess = {},
                                onError = { throwable ->
                                    dispatch(Msg.NotificationPermissionRequestFinished)
                                    publish(
                                        Label.ErrorCaught(
                                            BlinklyError.NotificationPermissionRequesting(throwable)
                                        )
                                    )
                                },
                            )
                        }
                    }
                }

                onIntent<Intent.DeleteReminder> { intent ->
                    if (state().pendingDeleteUuid != null) return@onIntent

                    val reminder = state().reminders.firstOrNull { it.uuid == intent.uuid } ?: return@onIntent
                    dispatch(Msg.DeleteStarted(intent.uuid))

                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.deleteReminder(intent.uuid) },
                            onSuccess = {
                                if (state().pendingDeleteUuid == intent.uuid) {
                                    dispatch(Msg.ReminderDeleted(reminder))
                                }
                            },
                            onError = { throwable ->
                                dispatch(Msg.DeleteFinished)
                                publish(Label.ErrorCaught(BlinklyError.ReminderDeleting(throwable)))
                            },
                        )
                    }
                }

                onIntent<Intent.UndoDelete> {
                    if (state().isRestoringDeleted) return@onIntent

                    val reminder = state().deletedReminder ?: return@onIntent
                    dispatch(Msg.RestoreStarted)

                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.restoreReminder(reminder) },
                            onSuccess = { dispatch(Msg.DeletedMessageShown) },
                            onError = { throwable ->
                                dispatch(Msg.RestoreFinished)
                                publish(Label.ErrorCaught(BlinklyError.ReminderRestoring(throwable)))
                            },
                        )
                    }
                }

                onIntent<Intent.DeleteMessageShown> {
                    dispatch(Msg.DeletedMessageShown)
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.RemindersUpdated -> copy(
                        reminders = msg.items,
                    )

                    is Msg.ReminderDeleted -> copy(
                        reminders = reminders.filterNot { it.uuid == msg.item.uuid },
                        deletedReminder = msg.item,
                        pendingDeleteUuid = null,
                    )

                    is Msg.DeletedMessageShown -> copy(
                        deletedReminder = null,
                        isRestoringDeleted = false,
                    )

                    is Msg.DeleteStarted -> copy(
                        pendingDeleteUuid = msg.uuid,
                    )

                    is Msg.DeleteFinished -> copy(
                        pendingDeleteUuid = null,
                    )

                    is Msg.RestoreStarted -> copy(
                        isRestoringDeleted = true,
                    )

                    is Msg.RestoreFinished -> copy(
                        isRestoringDeleted = false,
                    )

                    is Msg.NotificationPermissionRequestStarted -> copy(
                        isRequestingNotificationPermission = true,
                    )

                    is Msg.NotificationPermissionRequestFinished -> copy(
                        isRequestingNotificationPermission = false,
                    )
                }
            },
        ) {}

    sealed interface Action {
        data object ObserveReminders : Action
        data object ObserveNotificationPermission : Action
    }

    sealed interface Msg {
        data class RemindersUpdated(val items: List<Reminder>) : Msg
        data class ReminderDeleted(val item: Reminder) : Msg
        data object DeletedMessageShown : Msg
        data class DeleteStarted(val uuid: String) : Msg
        data object DeleteFinished : Msg
        data object RestoreStarted : Msg
        data object RestoreFinished : Msg
        data object NotificationPermissionRequestStarted : Msg
        data object NotificationPermissionRequestFinished : Msg
    }
}
