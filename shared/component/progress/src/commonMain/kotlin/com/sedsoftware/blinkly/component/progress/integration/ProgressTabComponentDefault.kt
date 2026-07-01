package com.sedsoftware.blinkly.component.progress.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent.Model
import com.sedsoftware.blinkly.component.progress.domain.ProgressTabManager
import com.sedsoftware.blinkly.component.progress.store.ProgressTabStore
import com.sedsoftware.blinkly.component.progress.store.ProgressTabStoreProvider
import com.sedsoftware.blinkly.domain.BlinklyAchievementsWatcher
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class ProgressTabComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val timeUtils: BlinklyTimeUtils,
    private val calendarWatcher: BlinklyCalendarWatcher,
    private val achievementsWatcher: BlinklyAchievementsWatcher,
    private val treeProgressWatcher: BlinklyTreeProgressWatcher,
    private val progressTabOutput: (ComponentOutput) -> Unit,
) : ProgressTabComponent, ComponentContext by componentContext {

    private val store: ProgressTabStore =
        instanceKeeper.getStore {
            ProgressTabStoreProvider(
                storeFactory = storeFactory,
                manager = ProgressTabManager(
                    calendarWatcher = calendarWatcher,
                    achievementsWatcher = achievementsWatcher,
                    treeProgressWatcher = treeProgressWatcher,
                    timeUtils = timeUtils,
                ),
                mainContext = dispatchers.main,
                ioContext = dispatchers.io,
            ).create(autoInit = false)
        }

    init {
        val scope = CoroutineScope(dispatchers.main)

        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            store.labels.collect { label ->
                when (label) {
                    is ProgressTabStore.Label.ErrorCaught -> {
                        progressTabOutput(ComponentOutput.Common.ErrorCaught(label.exception))
                    }
                }
            }
        }

        store.init()

        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onAchievementsClick() {
        progressTabOutput(ComponentOutput.Progress.OpenAchievements)
    }

    override fun onGardenClick() {
        progressTabOutput(ComponentOutput.Progress.OpenGarden)
    }
}
