package com.sedsoftware.blinkly.domain.internal

import com.sedsoftware.blinkly.domain.ExerciseManager
import com.sedsoftware.blinkly.domain.exercise.dsl.CompleteBlockNode
import com.sedsoftware.blinkly.domain.exercise.dsl.CompleteExerciseNode
import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseNode
import com.sedsoftware.blinkly.domain.exercise.dsl.MovementNode
import com.sedsoftware.blinkly.domain.exercise.dsl.TickNode
import com.sedsoftware.blinkly.domain.exercise.engine.ExerciseEngine
import com.sedsoftware.blinkly.domain.exercise.scripts.factory.ScriptFactory
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseEvent
import com.sedsoftware.blinkly.domain.model.ExerciseProgress
import com.sedsoftware.blinkly.domain.model.ExerciseType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal class ExerciseManagerImpl(
    private val database: BlinklyDatabase,
    private val settings: BlinklySettings,
    private val timeUtils: BlinklyTimeUtils,
    dispatchers: BlinklyDispatchers,
) : ExerciseManager {

    private val engine: ExerciseEngine = ExerciseEngine(dispatchers)
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.main)
    private val _events: MutableSharedFlow<ExerciseEvent> = MutableSharedFlow<ExerciseEvent>()

    override val events: Flow<ExerciseEvent> = _events

    private var paused: Boolean = false
    private var currentBlock: ExerciseBlock? = null
    private var exerciseIndex: Int = 0

    override fun pause() {
        paused = true
    }

    override fun resume() {
        paused = false
    }

    override fun stop() {
        scope.coroutineContext.cancelChildren()
    }

    override fun startBlock(block: ExerciseBlock) {
        currentBlock = block
        exerciseIndex = 0
    }

    override fun startNextExercise() {
        val block = currentBlock ?: return
        val exercises = blockExercises(block)
        val type = exercises.getOrNull(exerciseIndex) ?: return

        exerciseIndex++

        scope.launch {
            runExercise(block, type)
        }
    }

    private suspend fun runExercise(
        block: ExerciseBlock,
        type: ExerciseType,
    ) {
        val script = ScriptFactory.create(type, settings)

        engine.run(
            script = script,
            emitter = { node, progress ->
                handleNode(block, type, node, progress)
            },
            isPaused = { paused }
        )
    }

    private suspend fun handleNode(
        block: ExerciseBlock,
        type: ExerciseType,
        node: ExerciseNode,
        progress: ExerciseProgress?,
    ) {
        progress?.let {
            _events.emit(
                ExerciseEvent.Progress(block, type, it)
            )
        }

        when (node) {
            is MovementNode -> {
                _events.emit(ExerciseEvent.Movement(block, type, node.movement))
            }

            is TickNode -> {
                _events.emit(ExerciseEvent.Tick(block, type, node.second))
            }

            CompleteExerciseNode -> {
                database.saveExercise(Exercise(block, type, timeUtils.now()))
                _events.emit(ExerciseEvent.ExerciseCompleted(block, type))
            }

            CompleteBlockNode -> {
                _events.emit(ExerciseEvent.BlockCompleted(block))
            }
        }
    }

    private fun blockExercises(block: ExerciseBlock) =
        when (block) {
            ExerciseBlock.A -> listOf(
                ExerciseType.BLINK_BREAK,
                ExerciseType.NEAR_FAR_FOCUS,
                ExerciseType.DIAGONAL_GAZES
            )

            ExerciseBlock.B -> listOf(
                ExerciseType.FIGURE_EIGHT,
                ExerciseType.CLOCK_ROLLS,
                ExerciseType.PALMING
            )

            ExerciseBlock.C -> listOf(
                ExerciseType.TWENTY_X3
            )
        }
}
