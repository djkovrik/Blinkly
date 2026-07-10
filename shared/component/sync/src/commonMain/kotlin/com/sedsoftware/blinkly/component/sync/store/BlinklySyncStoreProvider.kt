package com.sedsoftware.blinkly.component.sync.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.sync.store.BlinklySyncStore.Intent
import com.sedsoftware.blinkly.component.sync.store.BlinklySyncStore.Label
import com.sedsoftware.blinkly.component.sync.store.BlinklySyncStore.State
import com.sedsoftware.blinkly.domain.external.BlinklySyncManager
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.BlinklySyncState
import com.sedsoftware.blinkly.domain.model.asBlinklyError
import com.sedsoftware.blinkly.utils.StoreProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class BlinklySyncStoreProvider(
    private val storeFactory: StoreFactory,
    private val syncManager: BlinklySyncManager,
    private val mainContext: CoroutineContext,
) {

    @StoreProvider
    fun create(autoInit: Boolean = true): BlinklySyncStore =
        object : BlinklySyncStore,
            com.arkivanov.mvikotlin.core.store.Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
                name = "BlinklySyncStore",
                initialState = State(),
                autoInit = autoInit,
                bootstrapper = coroutineBootstrapper(mainContext) {
                    dispatch(Action.ObserveSyncState)
                },
                executorFactory = coroutineExecutorFactory(mainContext) {
                    onAction<Action.ObserveSyncState> {
                        launch {
                            syncManager.state
                                .catch { throwable ->
                                    val error = throwable.asBlinklyError(BlinklyError::SyncUnknown)
                                    dispatch(Msg.SyncFailed(error))
                                    publish(Label.ErrorCaught(error))
                                }
                                .collect { syncState -> dispatch(Msg.SyncStateChanged(syncState)) }
                        }
                    }

                    onIntent<Intent.PrimaryButtonClicked> {
                        if (state().isAuthorized) {
                            launch {
                                runCatching { syncManager.syncNow() }
                                    .onFailure { throwable ->
                                        val error = throwable.asBlinklyError(BlinklyError::SyncUnknown)
                                        dispatch(Msg.SyncFailed(error))
                                        publish(Label.ErrorCaught(error))
                                    }
                            }
                        }
                    }

                    onIntent<Intent.GoogleSignInCompleted> { intent ->
                        launch {
                            runCatching { syncManager.completeGoogleSignIn(intent.user) }
                                .onFailure { throwable ->
                                    val error = throwable.asBlinklyError(BlinklyError::SyncAuthFailed)
                                    dispatch(Msg.SyncFailed(error))
                                    publish(Label.ErrorCaught(error))
                                }
                        }
                    }

                    onIntent<Intent.GoogleSignInFailed> { intent ->
                        val error = intent.throwable.asBlinklyError(BlinklyError::SyncAuthFailed)
                        dispatch(Msg.SyncFailed(error))
                        publish(Label.ErrorCaught(error))
                    }
                },
                reducer = ReducerImpl,
            ) {}

    private sealed interface Action {
        data object ObserveSyncState : Action
    }

    private sealed interface Msg {
        data class SyncStateChanged(val state: BlinklySyncState) : Msg
        data class SyncFailed(val error: BlinklyError) : Msg
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.SyncStateChanged ->
                    copy(
                        isAuthorized = msg.state.isAuthorized,
                        isSyncing = msg.state.isSyncing,
                        lastSyncedAt = msg.state.lastSyncedAt,
                        error = msg.state.error,
                    )

                is Msg.SyncFailed ->
                    copy(
                        isSyncing = false,
                        error = msg.error,
                    )
            }
    }
}
