package com.sedsoftware.blinkly.component.workout.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.workout.WorkoutComponent
import com.sedsoftware.blinkly.component.workout.WorkoutComponent.Model
import com.sedsoftware.blinkly.component.workout.store.WorkoutStore
import com.sedsoftware.blinkly.component.workout.store.WorkoutStoreProvider
import com.sedsoftware.blinkly.domain.BlinklyExerciseManager
import com.sedsoftware.blinkly.domain.BlinklyAnalytics
import com.sedsoftware.blinkly.domain.NoOpBlinklyAnalytics
import com.sedsoftware.blinkly.domain.external.BlinklyBeeper
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyScreenAwakeController
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.BlinklyAnalyticsEvent
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.TrainingSource
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class WorkoutComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val block: ExerciseBlock,
    private val source: TrainingSource = TrainingSource.TRAININGS,
    private val analytics: BlinklyAnalytics = NoOpBlinklyAnalytics,
    private val exerciseManager: BlinklyExerciseManager,
    private val beeper: BlinklyBeeper,
    private val screenAwakeController: BlinklyScreenAwakeController,
    private val workoutOutput: (ComponentOutput) -> Unit,
) : WorkoutComponent, ComponentContext by componentContext {

    private var workoutStarted = false
    private var workoutCompleted = false
    private var cancellationReported = false

    private val store: WorkoutStore =
        instanceKeeper.getStore {
            WorkoutStoreProvider(
                storeFactory = storeFactory,
                block = block,
                exerciseManager = exerciseManager,
                beeper = beeper,
                mainContext = dispatchers.main,
            ).create(autoInit = false)
        }

    init {
        val scope = CoroutineScope(dispatchers.main)

        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            store.labels.collect { label ->
                when (label) {
                    WorkoutStore.Label.WorkoutStarted -> {
                        if (!workoutStarted) {
                            workoutStarted = true
                            analytics.report(BlinklyAnalyticsEvent.TrainingStarted(source))
                        }
                    }
                    WorkoutStore.Label.WorkoutCompleted -> {
                        if (!workoutCompleted) {
                            workoutCompleted = true
                            analytics.report(
                                BlinklyAnalyticsEvent.TrainingFinished(BlinklyAnalyticsEvent.TrainingResult.COMPLETED)
                            )
                        }
                    }
                    is WorkoutStore.Label.ErrorCaught -> {
                        workoutOutput(ComponentOutput.Common.ErrorCaught(label.exception))
                    }
                }
            }
        }

        store.init()

        lifecycle.doOnResume {
            screenAwakeController.enable()
        }

        lifecycle.doOnPause {
            screenAwakeController.disable()
            store.accept(WorkoutStore.Intent.AppPaused)
        }

        lifecycle.doOnDestroy {
            reportCancellationIfNeeded()
            screenAwakeController.disable()
            exerciseManager.stop()
            scope.cancel()
        }
    }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onBackClick() {
        reportCancellationIfNeeded()
        workoutOutput(ComponentOutput.Common.BackPressed)
    }

    override fun onStartClick() {
        store.accept(WorkoutStore.Intent.StartClicked)
    }

    override fun onPauseClick() {
        store.accept(WorkoutStore.Intent.PauseClicked)
    }

    override fun onResumeClick() {
        store.accept(WorkoutStore.Intent.ResumeClicked)
    }

    override fun onFinishClick() {
        reportCancellationIfNeeded()
        workoutOutput(ComponentOutput.Common.BackPressed)
    }

    private fun reportCancellationIfNeeded() {
        if (workoutStarted && !workoutCompleted && !cancellationReported) {
            cancellationReported = true
            analytics.report(
                BlinklyAnalyticsEvent.TrainingFinished(BlinklyAnalyticsEvent.TrainingResult.CANCELLED)
            )
        }
    }
}
