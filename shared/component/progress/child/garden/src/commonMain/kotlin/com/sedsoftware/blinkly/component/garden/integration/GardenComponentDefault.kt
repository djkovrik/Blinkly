package com.sedsoftware.blinkly.component.garden.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.garden.GardenComponent
import com.sedsoftware.blinkly.component.garden.GardenComponent.Model
import com.sedsoftware.blinkly.component.garden.store.GardenStore
import com.sedsoftware.blinkly.component.garden.store.GardenStoreProvider
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.TreeType
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class GardenComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val treeProgressWatcher: BlinklyTreeProgressWatcher,
    private val gardenOutput: (ComponentOutput) -> Unit,
) : GardenComponent, ComponentContext by componentContext {

    private val store: GardenStore =
        instanceKeeper.getStore {
            GardenStoreProvider(
                storeFactory = storeFactory,
                treeProgressWatcher = treeProgressWatcher,
                mainContext = dispatchers.main,
            ).create(autoInit = false)
        }

    init {
        val scope = CoroutineScope(dispatchers.main)

        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            store.labels.collect { label ->
                when (label) {
                    is GardenStore.Label.ErrorCaught ->
                        gardenOutput(ComponentOutput.Common.ErrorCaught(label.exception))
                }
            }
        }

        store.init()

        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onBackClick() {
        gardenOutput(ComponentOutput.Common.BackPressed)
    }

    override fun onTreeClick(type: TreeType) {
        store.accept(GardenStore.Intent.TreeClicked(type))
    }

    override fun onTreeDetailsDismiss() {
        store.accept(GardenStore.Intent.TreeDetailsDismissed)
    }
}
