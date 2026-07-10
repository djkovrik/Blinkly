package com.sedsoftware.blinkly.component.sync.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.sync.BlinklySyncComponent
import com.sedsoftware.blinkly.component.sync.BlinklySyncComponent.Model
import com.sedsoftware.blinkly.component.sync.store.BlinklySyncStore
import com.sedsoftware.blinkly.component.sync.store.BlinklySyncStoreProvider
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklySyncManager
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BlinklySyncComponentDefault(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: BlinklyDispatchers,
    syncManager: BlinklySyncManager,
    private val syncOutput: (ComponentOutput) -> Unit,
) : BlinklySyncComponent, ComponentContext by componentContext {

    private val store: BlinklySyncStore =
        instanceKeeper.getStore {
            BlinklySyncStoreProvider(
                storeFactory = storeFactory,
                syncManager = syncManager,
                mainContext = dispatchers.main,
            ).create(autoInit = false)
        }

    init {
        val scope = CoroutineScope(dispatchers.main)

        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            store.labels.collect { label ->
                when (label) {
                    is BlinklySyncStore.Label.ErrorCaught ->
                        syncOutput(ComponentOutput.Common.ErrorCaught(label.exception))
                }
            }
        }

        store.init()

        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onPrimaryButtonClick() {
        store.accept(BlinklySyncStore.Intent.PrimaryButtonClicked)
    }

    override fun onGoogleSignInCompleted(user: BlinklyUser) {
        store.accept(BlinklySyncStore.Intent.GoogleSignInCompleted(user))
    }

    override fun onGoogleSignInFailed(throwable: Throwable) {
        store.accept(BlinklySyncStore.Intent.GoogleSignInFailed(throwable))
    }
}
