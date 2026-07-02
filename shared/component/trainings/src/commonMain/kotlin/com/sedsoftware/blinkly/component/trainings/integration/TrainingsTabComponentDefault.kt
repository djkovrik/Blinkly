package com.sedsoftware.blinkly.component.trainings.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent.Model
import com.sedsoftware.blinkly.component.trainings.domain.TrainingsTabManager
import com.sedsoftware.blinkly.component.trainings.store.TrainingsTabStore
import com.sedsoftware.blinkly.component.trainings.store.TrainingsTabStoreProvider
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TrainingsTabComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val timeUtils: BlinklyTimeUtils,
    private val calendarWatcher: BlinklyCalendarWatcher,
    private val trainingsTabOutput: (ComponentOutput) -> Unit,
) : TrainingsTabComponent, ComponentContext by componentContext {

    private val store: TrainingsTabStore =
        instanceKeeper.getStore {
            TrainingsTabStoreProvider(
                storeFactory = storeFactory,
                manager = TrainingsTabManager(
                    calendarWatcher = calendarWatcher,
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
                    is TrainingsTabStore.Label.ErrorCaught -> {
                        trainingsTabOutput(ComponentOutput.Common.ErrorCaught(label.exception))
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

    override fun onBlockAClick() {
        trainingsTabOutput(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.A))
    }

    override fun onBlockBClick() {
        trainingsTabOutput(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.B))
    }

    override fun onBlockCClick() {
        trainingsTabOutput(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.C))
    }
}
