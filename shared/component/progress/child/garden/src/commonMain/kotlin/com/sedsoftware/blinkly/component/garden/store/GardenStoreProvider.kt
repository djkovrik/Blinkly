package com.sedsoftware.blinkly.component.garden.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.garden.store.GardenStore.Intent
import com.sedsoftware.blinkly.component.garden.store.GardenStore.Label
import com.sedsoftware.blinkly.component.garden.store.GardenStore.State
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.model.TreeGarden
import com.sedsoftware.blinkly.domain.model.TreeType
import com.sedsoftware.blinkly.utils.StoreProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class GardenStoreProvider(
    private val storeFactory: StoreFactory,
    private val treeProgressWatcher: BlinklyTreeProgressWatcher,
    private val mainContext: CoroutineContext,
) {
    @StoreProvider
    fun create(autoInit: Boolean = true): GardenStore =
        object : GardenStore, Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
            name = "GardenStore",
            initialState = State(),
            autoInit = autoInit,
            bootstrapper = coroutineBootstrapper(mainContext) {
                dispatch(Action.ObserveGarden)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                onAction<Action.ObserveGarden> {
                    launch {
                        treeProgressWatcher.garden
                            .catch { publish(Label.ErrorCaught(it)) }
                            .collect { garden ->
                                dispatch(Msg.GardenUpdated(garden))
                            }
                    }
                }

                onIntent<Intent.TreeClicked> {
                    dispatch(Msg.TreeSelected(it.type))
                }

                onIntent<Intent.TreeDetailsDismissed> {
                    dispatch(Msg.TreeDetailsDismissed)
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.GardenUpdated -> copy(
                        garden = msg.garden,
                        selectedType = selectedType?.takeIf { type ->
                            msg.garden.grownTrees.any { tree -> tree.type == type }
                        },
                    )

                    is Msg.TreeSelected -> {
                        if (garden.grownTrees.any { tree -> tree.type == msg.type }) {
                            copy(selectedType = msg.type)
                        } else {
                            this
                        }
                    }

                    is Msg.TreeDetailsDismissed -> copy(
                        selectedType = null,
                    )
                }
            }
        ) {}

    sealed interface Action {
        data object ObserveGarden : Action
    }

    sealed interface Msg {
        data class GardenUpdated(val garden: TreeGarden) : Msg
        data class TreeSelected(val type: TreeType) : Msg
        data object TreeDetailsDismissed : Msg
    }
}
