package com.sedsoftware.blinkly.component.trainings.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.trainings.domain.TrainingsTabManager
import com.sedsoftware.blinkly.component.trainings.store.TrainingsTabStore.Intent
import com.sedsoftware.blinkly.component.trainings.store.TrainingsTabStore.Label
import com.sedsoftware.blinkly.component.trainings.store.TrainingsTabStore.State
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.utils.StoreProvider
import com.sedsoftware.blinkly.utils.unwrap
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class TrainingsTabStoreProvider(
    private val storeFactory: StoreFactory,
    private val manager: TrainingsTabManager,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
) {
    @StoreProvider
    fun create(autoInit: Boolean = true): TrainingsTabStore =
        object : TrainingsTabStore, Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
            name = "TrainingsTabStore",
            initialState = State(),
            autoInit = autoInit,
            bootstrapper = coroutineBootstrapper(mainContext) {
                dispatch(Action.ObserveCalendar)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                onAction<Action.ObserveCalendar> {
                    launch {
                        manager.calendar
                            .catch { publish(Label.ErrorCaught(it)) }
                            .collect { calendar ->
                                unwrap(
                                    result = withContext(ioContext) { manager.completedToday(calendar) },
                                    onSuccess = { completed -> dispatch(Msg.CompletedTodayUpdated(completed)) },
                                    onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                                )
                            }
                    }
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.CompletedTodayUpdated -> copy(completedToday = msg.completedToday)
                }
            },
        ) {}

    sealed interface Action {
        data object ObserveCalendar : Action
    }

    sealed interface Msg {
        data class CompletedTodayUpdated(val completedToday: Set<ExerciseBlock>) : Msg
    }
}
