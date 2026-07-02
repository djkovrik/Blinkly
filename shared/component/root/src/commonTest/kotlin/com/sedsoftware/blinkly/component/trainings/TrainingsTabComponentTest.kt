@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.component.trainings

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.trainings.integration.TrainingsTabComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.time.Instant

class TrainingsTabComponentTest : ComponentTest<TrainingsTabComponent>() {

    private val calendarFlow: MutableStateFlow<List<Workout>> = MutableStateFlow(emptyList())
    private val timeUtils: FakeTimeUtils = FakeTimeUtils()
    private var calendarWatcher: BlinklyCalendarWatcher = object : BlinklyCalendarWatcher {
        override val calendar: Flow<List<Workout>> = calendarFlow
    }

    @Test
    fun `when component created then model contains all training cards in catalog order`() = runTest(testScheduler) {
        // when
        testScheduler.advanceUntilIdle()

        // then
        val cards = component.model.value.cards
        assertThat(cards.map { it.block }).isEqualTo(
            listOf(
                ExerciseBlock.A,
                ExerciseBlock.C,
                ExerciseBlock.B,
            )
        )
        assertThat(cards.any { it.completedToday }).isFalse()
    }

    @Test
    fun `when exercises completed today then matching cards are marked completed`() = runTest(testScheduler) {
        // given
        calendarFlow.value = listOf(
            Workout(
                exercises = listOf(
                    exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, day = 15),
                    exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS, day = 15),
                    exercise(ExerciseBlock.A, ExerciseType.DIAGONAL_GAZES, day = 15),
                    exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, day = 15),
                )
            )
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        val cards = component.model.value.cards
        assertThat(cards.first { it.block == ExerciseBlock.A }.completedToday).isTrue()
        assertThat(cards.first { it.block == ExerciseBlock.C }.completedToday).isTrue()
        assertThat(cards.first { it.block == ExerciseBlock.B }.completedToday).isFalse()
    }

    @Test
    fun `when only part of a full block was completed today then card stays not completed`() = runTest(testScheduler) {
        // given
        calendarFlow.value = listOf(
            Workout(
                exercises = listOf(
                    exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, day = 15),
                    exercise(ExerciseBlock.B, ExerciseType.PALMING, day = 15),
                )
            )
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.cards.first { it.block == ExerciseBlock.A }.completedToday).isFalse()
        assertThat(component.model.value.cards.first { it.block == ExerciseBlock.B }.completedToday).isFalse()
    }

    @Test
    fun `when exercises were completed before today then cards stay not completed`() = runTest(testScheduler) {
        // given
        calendarFlow.value = listOf(
            Workout(
                exercises = listOf(
                    exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, day = 14),
                    exercise(ExerciseBlock.B, ExerciseType.PALMING, day = 14),
                )
            )
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.cards.any { it.completedToday }).isFalse()
    }

    @Test
    fun `when local time zone changes the today calculation uses local date`() = runTest(testScheduler) {
        // given
        val timeZone = TimeZone.of("Europe/Moscow")
        timeUtils.zone = timeZone
        timeUtils.current = instantAt(day = 16, hour = 1, minute = 0, timeZone = timeZone)
        calendarFlow.value = listOf(
            Workout(
                exercises = listOf(
                    exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT, day = 16, hour = 0, minute = 20, timeZone = timeZone),
                    exercise(ExerciseBlock.B, ExerciseType.CLOCK_ROLLS, day = 16, hour = 0, minute = 25, timeZone = timeZone),
                    exercise(ExerciseBlock.B, ExerciseType.PALMING, day = 16, hour = 0, minute = 30, timeZone = timeZone),
                )
            )
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.cards.first { it.block == ExerciseBlock.B }.completedToday).isTrue()
    }

    @Test
    fun `when training clicked then output opens matching exercise block`() = runTest(testScheduler) {
        // when
        component.onBlockAClick()
        component.onBlockBClick()
        component.onBlockCClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.A))
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.B))
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.C))
    }

    @Test
    fun `when calendar watcher fails then component publishes error output`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("calendar failed")
        calendarWatcher = object : BlinklyCalendarWatcher {
            override val calendar: Flow<List<Workout>> = flow { throw exception }
        }
        val testComponent = createComponent()

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(testComponent.model.value.cards.any { it.completedToday }).isFalse()
        assertThat(
            componentOutput.filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable.message == exception.message }
        ).isTrue()
    }

    override fun createComponent(): TrainingsTabComponent =
        TrainingsTabComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            timeUtils = timeUtils,
            calendarWatcher = calendarWatcher,
            trainingsTabOutput = { componentOutput.add(it) },
        )

    private fun exercise(
        block: ExerciseBlock,
        type: ExerciseType,
        day: Int,
        hour: Int = 12,
        minute: Int = 0,
        timeZone: TimeZone = TimeZone.UTC,
    ): Exercise =
        Exercise(
            block = block,
            type = type,
            completedAt = instantAt(day = day, hour = hour, minute = minute, timeZone = timeZone),
        )

    private fun instantAt(
        day: Int = 15,
        hour: Int,
        minute: Int,
        timeZone: TimeZone = TimeZone.UTC,
    ): Instant =
        LocalDateTime(year = 2026, month = 3, day = day, hour = hour, minute = minute).toInstant(timeZone)

    private class FakeTimeUtils : BlinklyTimeUtils {
        var current: Instant = LocalDateTime(year = 2026, month = 3, day = 15, hour = 12, minute = 0).toInstant(TimeZone.UTC)
        var zone: TimeZone = TimeZone.UTC

        override fun now(): Instant = current
        override fun timeZone(): TimeZone = zone
    }
}
