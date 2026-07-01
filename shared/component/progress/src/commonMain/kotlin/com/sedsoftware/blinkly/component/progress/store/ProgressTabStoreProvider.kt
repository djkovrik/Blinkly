package com.sedsoftware.blinkly.component.progress.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.progress.domain.ProgressTabManager
import com.sedsoftware.blinkly.component.progress.domain.model.ProgressCalendarDay
import com.sedsoftware.blinkly.component.progress.store.ProgressTabStore.Intent
import com.sedsoftware.blinkly.component.progress.store.ProgressTabStore.Label
import com.sedsoftware.blinkly.component.progress.store.ProgressTabStore.State
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.utils.StoreProvider
import com.sedsoftware.blinkly.utils.unwrap
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class ProgressTabStoreProvider(
    private val storeFactory: StoreFactory,
    private val manager: ProgressTabManager,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
) {
    @StoreProvider
    fun create(autoInit: Boolean = true): ProgressTabStore =
        object : ProgressTabStore, Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
            name = "ProgressTabStore",
            initialState = State(),
            autoInit = autoInit,
            bootstrapper = coroutineBootstrapper(mainContext) {
                dispatch(Action.ObserveCalendar)
                dispatch(Action.ObserveTree)
                dispatch(Action.ObserveAchievements)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                onAction<Action.ObserveCalendar> {
                    launch {
                        manager.calendar
                            .catch { publish(Label.ErrorCaught(it)) }
                            .collect { calendar ->
                                unwrap(
                                    result = withContext(ioContext) { manager.calculateCalendarWeeks(calendar) },
                                    onSuccess = { weeks -> dispatch(Msg.CalendarUpdated(weeks)) },
                                    onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                                )
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

                onAction<Action.ObserveAchievements> {
                    launch {
                        manager.achievements
                            .catch { publish(Label.ErrorCaught(it)) }
                            .collect { achievements ->
                                unwrap(
                                    result = withContext(ioContext) { manager.calculateRecentAchievements(achievements) },
                                    onSuccess = { recent -> dispatch(Msg.AchievementsUpdated(recent)) },
                                    onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                                )
                            }
                    }
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.CalendarUpdated -> copy(calendarWeeks = msg.calendarWeeks)
                    is Msg.TreeUpdated -> copy(tree = msg.tree)
                    is Msg.AchievementsUpdated -> copy(recentAchievements = msg.achievements)
                }
            },
        ) {}

    sealed interface Action {
        data object ObserveCalendar : Action
        data object ObserveTree : Action
        data object ObserveAchievements : Action
    }

    sealed interface Msg {
        data class CalendarUpdated(val calendarWeeks: List<List<ProgressCalendarDay?>>) : Msg
        data class TreeUpdated(val tree: Tree) : Msg
        data class AchievementsUpdated(val achievements: List<Achievement?>) : Msg
    }
}
