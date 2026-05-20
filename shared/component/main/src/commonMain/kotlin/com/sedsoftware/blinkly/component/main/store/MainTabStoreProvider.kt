package com.sedsoftware.blinkly.component.main.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.main.domain.MainTabManager
import com.sedsoftware.blinkly.component.main.domain.model.MainTabData
import com.sedsoftware.blinkly.component.main.store.MainTabStore.Intent
import com.sedsoftware.blinkly.component.main.store.MainTabStore.Label
import com.sedsoftware.blinkly.component.main.store.MainTabStore.State
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.utils.StoreProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class MainTabStoreProvider(
    private val storeFactory: StoreFactory,
    private val manager: MainTabManager,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
) {
    @StoreProvider
    fun create(autoInit: Boolean = true): MainTabStore =
        object : MainTabStore, Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
            name = "MainTabStore",
            initialState = State(),
            autoInit = autoInit,
            bootstrapper = coroutineBootstrapper(mainContext) {
                dispatch(Action.ObserveCalendar)
                dispatch(Action.ObserveTree)
                dispatch(Action.LoadHighlight)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                onAction<Action.ObserveCalendar> {
                    launch {
                        manager.calendar
                            .catch { publish(Label.ErrorCaught(it)) }
                            .collect { calendar ->
                                dispatch(Msg.CalendarUpdated(calendar))
                                val data = withContext(ioContext) {
                                    manager.calculateData(calendar)
                                }
                                dispatch(Msg.DataUpdated(data))
                            }
                    }
                }

                onAction<Action.ObserveTree> {
                    launch {
                        manager.tree
                            .catch { publish(Label.ErrorCaught(it)) }
                            .collect { tree ->
                                dispatch(Msg.TreeUpdated(tree))
                            }
                    }
                }

                onAction<Action.LoadHighlight> {
                    launch {
                        runCatching {
                            withContext(ioContext) { manager.getHighlight() }
                        }.onSuccess { highlight ->
                            dispatch(Msg.HighlightUpdated(highlight))
                        }.onFailure { throwable ->
                            publish(Label.ErrorCaught(throwable))
                        }
                    }
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.CalendarUpdated -> copy(
                        calendar = msg.calendar,
                    )

                    is Msg.HighlightUpdated -> copy(
                        highlight = msg.highlight,
                    )

                    is Msg.DataUpdated -> copy(
                        greetingPeriod = msg.data.greetingPeriod,
                        restMinutesToday = msg.data.restMinutesToday,
                        exercisesToday = msg.data.exercisesToday,
                        twentyX3Today = msg.data.twentyX3Today,
                        palmingToday = msg.data.palmingToday,
                        dailyProgressPercent = msg.data.dailyProgressPercent,
                        treeGrowthStreakDays = msg.data.treeGrowthStreakDays,
                        ctaState = msg.data.ctaState,
                    )

                    is Msg.TreeUpdated -> copy(
                        tree = msg.tree,
                    )
                }
            }
        ) {}

    sealed interface Action {
        data object ObserveCalendar : Action
        data object ObserveTree : Action
        data object LoadHighlight : Action
    }

    sealed interface Msg {
        data class CalendarUpdated(val calendar: List<Workout>) : Msg
        data class HighlightUpdated(val highlight: HighlightOfTheDay) : Msg
        data class DataUpdated(val data: MainTabData) : Msg
        data class TreeUpdated(val tree: Tree) : Msg
    }
}
