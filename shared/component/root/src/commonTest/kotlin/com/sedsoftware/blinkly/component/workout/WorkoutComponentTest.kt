package com.sedsoftware.blinkly.component.workout

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.workout.integration.WorkoutComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyExerciseManager
import com.sedsoftware.blinkly.domain.external.BlinklyBeeper
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseEvent
import com.sedsoftware.blinkly.domain.model.ExerciseProgress
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.EyeMovement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class WorkoutComponentTest : ComponentTest<WorkoutComponent>() {

    private var exerciseManager: FakeExerciseManager = FakeExerciseManager()
    private var beeper: FakeBeeper = FakeBeeper()

    @Test
    fun `when component created then intro model contains selected block exercises`() = runTest(testScheduler) {
        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.phase).isEqualTo(WorkoutComponent.Phase.INTRO)
        assertThat(component.model.value.block).isEqualTo(ExerciseBlock.A)
        assertThat(component.model.value.exercises).isEqualTo(
            listOf(
                ExerciseType.BLINK_BREAK,
                ExerciseType.NEAR_FAR_FOCUS,
                ExerciseType.DIAGONAL_GAZES,
            )
        )
        assertThat(component.model.value.currentExercise).isEqualTo(ExerciseType.BLINK_BREAK)
    }

    @Test
    fun `when start clicked from intro then block starts and first exercise runs`() = runTest(testScheduler) {
        // when
        component.onStartClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(exerciseManager.startedBlocks).contains(ExerciseBlock.A)
        assertThat(exerciseManager.startNextCount).isEqualTo(1)
        assertThat(component.model.value.phase).isEqualTo(WorkoutComponent.Phase.RUNNING)
        assertThat(component.model.value.currentExercise).isEqualTo(ExerciseType.BLINK_BREAK)
    }

    @Test
    fun `when manager emits exercise events then model and beeper are updated`() = runTest(testScheduler) {
        // given
        component.onStartClick()
        testScheduler.advanceUntilIdle()

        // when
        exerciseManager.emit(
            ExerciseEvent.Progress(
                block = ExerciseBlock.A,
                exercise = ExerciseType.BLINK_BREAK,
                progress = ExerciseProgress(percent = 40, remainingMs = 600L, totalMs = 1_000L),
            )
        )
        exerciseManager.emit(
            ExerciseEvent.Movement(
                block = ExerciseBlock.A,
                exercise = ExerciseType.BLINK_BREAK,
                movement = EyeMovement.Blink(count = 2),
            )
        )
        exerciseManager.emit(
            ExerciseEvent.Beep(
                block = ExerciseBlock.A,
                exercise = ExerciseType.BLINK_BREAK,
            )
        )
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.progress?.percent).isEqualTo(40)
        assertThat(component.model.value.movement).isEqualTo(EyeMovement.Blink(count = 2))
        assertThat(component.model.value.movementTrigger).isEqualTo(1)
        assertThat(beeper.beepCount).isEqualTo(1)
    }

    @Test
    fun `when timer tick arrives then timer is derived from progress`() = runTest(testScheduler) {
        // given
        val testComponent = createComponent(block = ExerciseBlock.C)
        testComponent.onStartClick()
        testScheduler.advanceUntilIdle()

        // when
        exerciseManager.emit(
            ExerciseEvent.Progress(
                block = ExerciseBlock.C,
                exercise = ExerciseType.TWENTY_X3,
                progress = ExerciseProgress(percent = 25, remainingMs = 15_000L, totalMs = 20_000L),
            )
        )
        exerciseManager.emit(
            ExerciseEvent.Tick(
                block = ExerciseBlock.C,
                exercise = ExerciseType.TWENTY_X3,
                second = 5,
            )
        )
        testScheduler.advanceUntilIdle()

        // then
        assertThat(testComponent.model.value.timerRemainingSeconds).isEqualTo(15)
    }

    @Test
    fun `when exercise completes then next exercise waits for manual start`() = runTest(testScheduler) {
        // given
        component.onStartClick()
        testScheduler.advanceUntilIdle()

        // when
        exerciseManager.emit(
            ExerciseEvent.ExerciseCompleted(
                block = ExerciseBlock.A,
                exercise = ExerciseType.BLINK_BREAK,
            )
        )
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.phase).isEqualTo(WorkoutComponent.Phase.READY)
        assertThat(component.model.value.currentExercise).isEqualTo(ExerciseType.NEAR_FAR_FOCUS)
        assertThat(exerciseManager.startNextCount).isEqualTo(1)

        // when
        component.onStartClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.phase).isEqualTo(WorkoutComponent.Phase.RUNNING)
        assertThat(exerciseManager.startNextCount).isEqualTo(2)
    }

    @Test
    fun `when block completes then final screen is shown and finish closes screen`() = runTest(testScheduler) {
        // given
        component.onStartClick()
        testScheduler.advanceUntilIdle()

        // when
        exerciseManager.emit(ExerciseEvent.BlockCompleted(block = ExerciseBlock.A))
        testScheduler.advanceUntilIdle()
        component.onFinishClick()

        // then
        assertThat(component.model.value.phase).isEqualTo(WorkoutComponent.Phase.COMPLETED)
        assertThat(componentOutput).contains(ComponentOutput.Common.BackPressed)
    }

    @Test
    fun `when pause and resume clicked then manager and model are updated`() = runTest(testScheduler) {
        // given
        component.onStartClick()
        testScheduler.advanceUntilIdle()

        // when
        component.onPauseClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(exerciseManager.pauseCount).isEqualTo(1)
        assertThat(component.model.value.phase).isEqualTo(WorkoutComponent.Phase.PAUSED)

        // when
        component.onResumeClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(exerciseManager.resumeCount).isEqualTo(1)
        assertThat(component.model.value.phase).isEqualTo(WorkoutComponent.Phase.RUNNING)
    }

    @Test
    fun `when event belongs to another block then it is ignored`() = runTest(testScheduler) {
        // given
        component.onStartClick()
        testScheduler.advanceUntilIdle()

        // when
        exerciseManager.emit(
            ExerciseEvent.Movement(
                block = ExerciseBlock.B,
                exercise = ExerciseType.PALMING,
                movement = EyeMovement.CircleClockwise,
            )
        )
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.movement).isNull()
        assertThat(component.model.value.movementTrigger).isEqualTo(0)
    }

    @Test
    fun `when event flow fails then component publishes error output`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("events failed")
        exerciseManager = FakeExerciseManager(eventsOverride = flow { throw exception })
        val testComponent = createComponent()

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(testComponent.model.value.phase).isEqualTo(WorkoutComponent.Phase.INTRO)
        assertThat(
            componentOutput.filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable.message == exception.message }
        ).isTrue()
    }

    @Test
    fun `when back clicked then component asks parent to close`() = runTest(testScheduler) {
        // when
        component.onBackClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Common.BackPressed)
    }

    @Test
    fun `when lifecycle destroyed then manager stops and beeper releases`() = runTest(testScheduler) {
        // given
        val localLifecycle = LifecycleRegistry()
        val localManager = FakeExerciseManager()
        val localBeeper = FakeBeeper()
        createComponent(
            lifecycle = localLifecycle,
            manager = localManager,
            beeper = localBeeper,
        )
        localLifecycle.create()
        localLifecycle.resume()

        // when
        localLifecycle.pause()
        localLifecycle.destroy()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(localManager.stopCount).isEqualTo(1)
        assertThat(localBeeper.releaseCount).isEqualTo(1)
    }

    override fun createComponent(): WorkoutComponent =
        createComponent(
            block = ExerciseBlock.A,
            lifecycle = lifecycle,
            manager = exerciseManager,
            beeper = beeper,
        )

    private fun createComponent(
        block: ExerciseBlock = ExerciseBlock.A,
        lifecycle: LifecycleRegistry = this.lifecycle,
        manager: FakeExerciseManager = exerciseManager,
        beeper: FakeBeeper = this.beeper,
    ): WorkoutComponent =
        WorkoutComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            block = block,
            exerciseManager = manager,
            beeper = beeper,
            workoutOutput = { componentOutput.add(it) },
        )

    private class FakeExerciseManager(
        eventsOverride: Flow<ExerciseEvent>? = null,
    ) : BlinklyExerciseManager {

        private val mutableEvents: MutableSharedFlow<ExerciseEvent> = MutableSharedFlow(extraBufferCapacity = BUFFER_CAPACITY)

        val startedBlocks: MutableList<ExerciseBlock> = mutableListOf()
        var startNextCount: Int = 0
        var pauseCount: Int = 0
        var resumeCount: Int = 0
        var stopCount: Int = 0

        override val events: Flow<ExerciseEvent> = eventsOverride ?: mutableEvents

        override fun startBlock(block: ExerciseBlock) {
            startedBlocks.add(block)
        }

        override fun startNextExercise() {
            startNextCount++
        }

        override fun pause() {
            pauseCount++
        }

        override fun resume() {
            resumeCount++
        }

        override fun stop() {
            stopCount++
        }

        fun emit(event: ExerciseEvent) {
            assertThat(mutableEvents.tryEmit(event)).isTrue()
        }

        private companion object {
            const val BUFFER_CAPACITY = 16
        }
    }

    private class FakeBeeper : BlinklyBeeper {
        var beepCount: Int = 0
        var releaseCount: Int = 0

        override fun beep() {
            beepCount++
        }

        override fun release() {
            releaseCount++
        }
    }
}
