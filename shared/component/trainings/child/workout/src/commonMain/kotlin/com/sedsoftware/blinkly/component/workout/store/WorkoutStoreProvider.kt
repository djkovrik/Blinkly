package com.sedsoftware.blinkly.component.workout.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.workout.store.WorkoutStore.Intent
import com.sedsoftware.blinkly.component.workout.store.WorkoutStore.Label
import com.sedsoftware.blinkly.component.workout.store.WorkoutStore.Phase
import com.sedsoftware.blinkly.component.workout.store.WorkoutStore.State
import com.sedsoftware.blinkly.domain.BlinklyExerciseManager
import com.sedsoftware.blinkly.domain.external.BlinklyBeeper
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseEvent
import com.sedsoftware.blinkly.domain.model.ExerciseProgress
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.EyeMovement
import com.sedsoftware.blinkly.domain.model.asBlinklyError
import com.sedsoftware.blinkly.utils.StoreProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class WorkoutStoreProvider(
    private val storeFactory: StoreFactory,
    private val block: ExerciseBlock,
    private val exerciseManager: BlinklyExerciseManager,
    private val beeper: BlinklyBeeper,
    private val mainContext: CoroutineContext,
) {

    @StoreProvider
    fun create(autoInit: Boolean = true): WorkoutStore {
        val exercises = block.exercises()

        return object : WorkoutStore, Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
            name = "WorkoutStore",
            initialState = State(
                block = block,
                exercises = exercises,
            ),
            autoInit = autoInit,
            bootstrapper = coroutineBootstrapper(mainContext) {
                dispatch(Action.ObserveEvents)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                onAction<Action.ObserveEvents> {
                    launch {
                        exerciseManager.events
                            .filter { event -> event.block == block }
                            .catch { throwable ->
                                publish(Label.ErrorCaught(throwable.asBlinklyError(BlinklyError::WorkoutDataLoading)))
                            }
                            .collect { event ->
                                when (event) {
                                    is ExerciseEvent.Movement -> dispatch(
                                        Msg.MovementUpdated(
                                            movement = event.movement,
                                            durationMs = event.durationMs,
                                        )
                                    )
                                    is ExerciseEvent.Progress -> dispatch(Msg.ProgressUpdated(event.progress))
                                    is ExerciseEvent.Tick -> dispatch(Msg.TickUpdated(event.second))
                                    is ExerciseEvent.Beep -> beeper.beep()
                                    is ExerciseEvent.ExerciseCompleted -> dispatch(Msg.ExerciseCompleted(event.exercise))
                                    is ExerciseEvent.BlockCompleted -> dispatch(Msg.BlockCompleted)
                                    is ExerciseEvent.Error -> publish(
                                        Label.ErrorCaught(event.throwable.asBlinklyError(BlinklyError::WorkoutDataLoading))
                                    )
                                }
                            }
                    }
                }

                onIntent<Intent.StartClicked> {
                    when (state().phase) {
                        Phase.INTRO -> {
                            exerciseManager.startBlock(block)
                            dispatch(Msg.ExerciseReady)
                        }

                        Phase.READY -> {
                            dispatch(Msg.ExerciseStarted)
                            exerciseManager.startNextExercise()
                        }

                        Phase.RUNNING,
                        Phase.PAUSED,
                        Phase.COMPLETED,
                            -> Unit
                    }
                }

                onIntent<Intent.PauseClicked> {
                    if (state().phase == Phase.RUNNING) {
                        exerciseManager.pause()
                        dispatch(Msg.ExercisePaused)
                    }
                }

                onIntent<Intent.AppPaused> {
                    if (state().phase == Phase.RUNNING) {
                        exerciseManager.pause()
                        dispatch(Msg.ExercisePaused)
                    }
                }

                onIntent<Intent.ResumeClicked> {
                    if (state().phase == Phase.PAUSED) {
                        exerciseManager.resume()
                        dispatch(Msg.ExerciseResumed)
                    }
                }
            },
            reducer = { msg ->
                when (msg) {
                    Msg.ExerciseReady -> copy(
                        phase = Phase.READY,
                        movement = null,
                        movementDurationMs = null,
                        progress = null,
                        timerElapsedSeconds = null,
                    )

                    Msg.ExerciseStarted -> copy(
                        phase = Phase.RUNNING,
                        movement = null,
                        movementDurationMs = null,
                        progress = null,
                        timerElapsedSeconds = null,
                    )

                    Msg.ExercisePaused -> copy(phase = Phase.PAUSED)
                    Msg.ExerciseResumed -> copy(phase = Phase.RUNNING)

                    is Msg.MovementUpdated -> copy(
                        movement = msg.movement,
                        movementDurationMs = msg.durationMs,
                        movementTrigger = movementTrigger + 1,
                    )

                    is Msg.ProgressUpdated -> copy(progress = msg.progress)

                    is Msg.TickUpdated -> copy(timerElapsedSeconds = msg.second)

                    is Msg.ExerciseCompleted -> {
                        val nextExerciseIndex = currentExerciseIndex + 1

                        if (nextExerciseIndex >= exercises.size) {
                            copy(
                                currentExerciseIndex = exercises.lastIndex.coerceAtLeast(0),
                                phase = Phase.COMPLETED,
                                movement = null,
                                movementDurationMs = null,
                                progress = progress?.copy(percent = FULL_PROGRESS),
                                timerElapsedSeconds = null,
                            )
                        } else {
                            copy(
                                currentExerciseIndex = nextExerciseIndex,
                                phase = Phase.READY,
                                movement = null,
                                movementDurationMs = null,
                                progress = null,
                                timerElapsedSeconds = null,
                            )
                        }
                    }

                    Msg.BlockCompleted -> copy(
                        phase = Phase.COMPLETED,
                        movement = null,
                        movementDurationMs = null,
                        progress = progress?.copy(percent = FULL_PROGRESS, remainingMs = 0L),
                        timerElapsedSeconds = null,
                    )
                }
            },
        ) {}
    }

    sealed interface Action {
        data object ObserveEvents : Action
    }

    sealed interface Msg {
        data object ExerciseReady : Msg
        data object ExerciseStarted : Msg
        data object ExercisePaused : Msg
        data object ExerciseResumed : Msg
        data class MovementUpdated(
            val movement: EyeMovement,
            val durationMs: Long,
        ) : Msg
        data class ProgressUpdated(val progress: ExerciseProgress) : Msg
        data class TickUpdated(val second: Int) : Msg
        data class ExerciseCompleted(val exercise: ExerciseType) : Msg
        data object BlockCompleted : Msg
    }

    private companion object {
        const val FULL_PROGRESS = 100
    }
}

internal fun ExerciseBlock.exercises(): List<ExerciseType> =
    when (this) {
        ExerciseBlock.A -> listOf(
            ExerciseType.BLINK_BREAK,
            ExerciseType.NEAR_FAR_FOCUS,
            ExerciseType.DIAGONAL_GAZES,
        )

        ExerciseBlock.B -> listOf(
            ExerciseType.FIGURE_EIGHT,
            ExerciseType.CLOCK_ROLLS,
            ExerciseType.PALMING,
        )

        ExerciseBlock.C -> listOf(
            ExerciseType.TWENTY_X3,
        )
    }
