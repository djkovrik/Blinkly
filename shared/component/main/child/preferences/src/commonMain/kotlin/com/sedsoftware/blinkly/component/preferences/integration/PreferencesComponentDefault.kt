package com.sedsoftware.blinkly.component.preferences.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.preferences.PreferencesComponent
import com.sedsoftware.blinkly.component.preferences.PreferencesComponent.Model
import com.sedsoftware.blinkly.component.preferences.domain.PreferencesManager
import com.sedsoftware.blinkly.component.preferences.store.PreferencesStore
import com.sedsoftware.blinkly.component.preferences.store.PreferencesStoreProvider
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PreferencesComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val settings: BlinklySettings,
    private val preferencesOutput: (ComponentOutput) -> Unit,
) : PreferencesComponent, ComponentContext by componentContext {

    private val store: PreferencesStore =
        instanceKeeper.getStore {
            PreferencesStoreProvider(
                storeFactory = storeFactory,
                manager = PreferencesManager(settings = settings),
                mainContext = dispatchers.main,
                ioContext = dispatchers.io,
            ).create(autoInit = false)
        }

    init {
        val scope = CoroutineScope(dispatchers.main)

        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is PreferencesStore.Label.ErrorCaught ->
                        preferencesOutput(ComponentOutput.Common.ErrorCaught(label.exception))

                    is PreferencesStore.Label.ThemeStateChanged ->
                        preferencesOutput(ComponentOutput.Preferences.ThemeStateChanged(label.value))
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
        preferencesOutput(ComponentOutput.Common.BackPressed)
    }

    override fun onBlinkBreakCountChanged(value: Int) {
        store.accept(PreferencesStore.Intent.BlinkBreakCountChanged(value))
    }

    override fun onNearFarFocusCountChanged(value: Int) {
        store.accept(PreferencesStore.Intent.NearFarFocusCountChanged(value))
    }

    override fun onNearFarFocusDurationChanged(value: Float) {
        store.accept(PreferencesStore.Intent.NearFarFocusDurationChanged(value))
    }

    override fun onDiagonalGazesCountChanged(value: Int) {
        store.accept(PreferencesStore.Intent.DiagonalGazesCountChanged(value))
    }

    override fun onDiagonalGazesDurationChanged(value: Float) {
        store.accept(PreferencesStore.Intent.DiagonalGazesDurationChanged(value))
    }

    override fun onFigureEightCountChanged(value: Int) {
        store.accept(PreferencesStore.Intent.FigureEightCountChanged(value))
    }

    override fun onClockRollsEachSideChanged(value: Int) {
        store.accept(PreferencesStore.Intent.ClockRollsEachSideChanged(value))
    }

    override fun onPalmingDurationChanged(value: Int) {
        store.accept(PreferencesStore.Intent.PalmingDurationChanged(value))
    }

    override fun onThemeStateChanged(value: ThemeState) {
        store.accept(PreferencesStore.Intent.ThemeStateChanged(value))
    }
}
