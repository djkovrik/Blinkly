package com.sedsoftware.blinkly.component.main.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.main.MainTabComponent
import com.sedsoftware.blinkly.component.main.MainTabComponent.Model
import com.sedsoftware.blinkly.component.main.domain.MainTabManager
import com.sedsoftware.blinkly.component.main.domain.model.MainCtaState
import com.sedsoftware.blinkly.component.main.store.MainTabStore
import com.sedsoftware.blinkly.component.main.store.MainTabStoreProvider
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyHighlightsProvider
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class MainTabComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val settings: BlinklySettings,
    private val timeUtils: BlinklyTimeUtils,
    private val calendarWatcher: BlinklyCalendarWatcher,
    private val highlightsProvider: BlinklyHighlightsProvider,
    private val treeProgressWatcher: BlinklyTreeProgressWatcher,
    private val mainTabOutput: (ComponentOutput) -> Unit,
) : MainTabComponent, ComponentContext by componentContext {

    private val store: MainTabStore =
        instanceKeeper.getStore {
            MainTabStoreProvider(
                storeFactory = storeFactory,
                manager = MainTabManager(
                    calendarWatcher = calendarWatcher,
                    treeProgressWatcher = treeProgressWatcher,
                    highlightsProvider = highlightsProvider,
                    settings = settings,
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
                    is MainTabStore.Label.ErrorCaught -> mainTabOutput(ComponentOutput.Common.ErrorCaught(label.exception))
                }
            }
        }

        store.init()

        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onPreferencesClick() {
        mainTabOutput(ComponentOutput.Main.OpenPreferences)
    }

    override fun onTreeClick() {
        mainTabOutput(ComponentOutput.Main.OpenProgressTab)
    }

    override fun onPrimaryCtaClick() {
        when (model.value.ctaState) {
            MainCtaState.MorningWarmUp,
            MainCtaState.AfternoonWarmUp,
                -> mainTabOutput(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.A))

            MainCtaState.EveningRelax -> mainTabOutput(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.B))
            MainCtaState.WorkBreakDue -> mainTabOutput(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.C))
            MainCtaState.DayClosing,
            MainCtaState.PerfectDay,
            MainCtaState.Idle,
                -> Unit
        }
    }
}
