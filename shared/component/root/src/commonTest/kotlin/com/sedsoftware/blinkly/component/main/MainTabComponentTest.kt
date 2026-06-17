package com.sedsoftware.blinkly.component.main

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.main.domain.model.GreetingPeriod
import com.sedsoftware.blinkly.component.main.domain.model.MainCtaState
import com.sedsoftware.blinkly.component.main.integration.MainTabComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyHighlightsProvider
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeGarden
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class MainTabComponentTest : ComponentTest<MainTabComponent>() {

    private val calendarFlow: MutableStateFlow<List<Workout>> = MutableStateFlow(emptyList())
    private val treeFlow: MutableStateFlow<Tree> = MutableStateFlow(DEFAULT_TREE)
    private val settings: FakeSettings = FakeSettings()
    private val timeUtils: FakeTimeUtils = FakeTimeUtils()
    private val highlightsProvider: FakeHighlightsProvider = FakeHighlightsProvider()

    @Test
    fun `when component created then model contains calendar tree and highlight data`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 14, minute = 0)
        treeFlow.value = Tree(TreeStage.SMALL, TreeType.GINKGO_BILOBA, 50f)
        calendarFlow.value = listOf(
            Workout(
                exercises = listOf(
                    exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, hour = 9, minute = 0),
                    exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS, hour = 9, minute = 5),
                    exercise(ExerciseBlock.A, ExerciseType.DIAGONAL_GAZES, hour = 9, minute = 10),
                    exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, hour = 12, minute = 0),
                    exercise(ExerciseBlock.B, ExerciseType.PALMING, hour = 13, minute = 0),
                )
            )
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        val model = component.model.value
        assertThat(model.greetingPeriod).isEqualTo(GreetingPeriod.DAY)
        assertThat(model.exercisesToday).isEqualTo(5)
        assertThat(model.twentyX3Today).isEqualTo(1)
        assertThat(model.palmingToday).isEqualTo(1)
        assertThat(model.dailyProgressPercent).isEqualTo(50)
        assertThat(model.restMinutesToday).isEqualTo(5)
        assertThat(model.tree).isEqualTo(Tree(TreeStage.SMALL, TreeType.GINKGO_BILOBA, 50f))
        assertThat(model.highlight).isEqualTo(HighlightOfTheDay.Tip(7))
    }

    @Test
    fun `when preferences clicked then output opens preferences`() = runTest(testScheduler) {
        // when
        component.onPreferencesClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Main.OpenPreferences)
    }

    @Test
    fun `when tree clicked then output opens progress tab`() = runTest(testScheduler) {
        // when
        component.onTreeClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Main.OpenProgressTab)
    }

    @Test
    fun `when morning and no activity then CTA suggests warm up`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 8, minute = 0)
        calendarFlow.value = emptyList()

        // when
        testScheduler.advanceUntilIdle()
        component.onPrimaryCtaClick()

        // then
        assertThat(component.model.value.ctaState).isEqualTo(MainCtaState.MorningWarmUp)
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.A))
    }

    @Test
    fun `when work break is due then CTA suggests twenty x3`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 12, minute = 0)
        calendarFlow.value = listOf(
            Workout(
                exercises = listOf(
                    exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, hour = 11, minute = 30),
                )
            )
        )

        // when
        testScheduler.advanceUntilIdle()
        component.onPrimaryCtaClick()

        // then
        assertThat(component.model.value.ctaState).isEqualTo(MainCtaState.WorkBreakDue)
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.C))
    }

    @Test
    fun `when clean install starts during work hours then CTA suggests twenty x3`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 12, minute = 0)
        calendarFlow.value = emptyList()

        // when
        testScheduler.advanceUntilIdle()
        component.onPrimaryCtaClick()

        // then
        assertThat(component.model.value.ctaState).isEqualTo(MainCtaState.WorkBreakDue)
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.C))
    }

    @Test
    fun `when work break is due in non UTC time zone then CTA uses local time`() = runTest(testScheduler) {
        // given
        val localTimeZone = TimeZone.of("Europe/Moscow")
        timeUtils.zone = localTimeZone
        timeUtils.current = instantAt(hour = 12, minute = 0, timeZone = localTimeZone)
        calendarFlow.value = listOf(
            Workout(
                exercises = listOf(
                    exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, hour = 11, minute = 30, timeZone = localTimeZone),
                )
            )
        )

        // when
        testScheduler.advanceUntilIdle()
        component.onPrimaryCtaClick()

        // then
        assertThat(component.model.value.greetingPeriod).isEqualTo(GreetingPeriod.DAY)
        assertThat(component.model.value.ctaState).isEqualTo(MainCtaState.WorkBreakDue)
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.C))
    }

    @Test
    fun `when afternoon and warm up is not completed after short breaks then CTA suggests warm up`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 14, minute = 0)
        calendarFlow.value = listOf(
            Workout(
                exercises = listOf(
                    exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, hour = 13, minute = 45),
                )
            )
        )

        // when
        testScheduler.advanceUntilIdle()
        component.onPrimaryCtaClick()

        // then
        assertThat(component.model.value.ctaState).isEqualTo(MainCtaState.AfternoonWarmUp)
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.A))
    }

    @Test
    fun `when evening and relax is not completed then CTA suggests relax`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 19, minute = 0)
        calendarFlow.value = listOf(
            Workout(
                exercises = listOf(
                    exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, hour = 9, minute = 0),
                )
            )
        )

        // when
        testScheduler.advanceUntilIdle()
        component.onPrimaryCtaClick()

        // then
        assertThat(component.model.value.ctaState).isEqualTo(MainCtaState.EveningRelax)
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.B))
    }

    @Test
    fun `when clean install starts in the evening then CTA suggests relax`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 19, minute = 0)
        calendarFlow.value = emptyList()

        // when
        testScheduler.advanceUntilIdle()
        component.onPrimaryCtaClick()

        // then
        assertThat(component.model.value.ctaState).isEqualTo(MainCtaState.EveningRelax)
        assertThat(componentOutput).contains(ComponentOutput.Trainings.OpenExerciseBlock(ExerciseBlock.B))
    }

    @Test
    fun `when late evening then CTA closes the day and does not navigate`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 22, minute = 30)
        calendarFlow.value = listOf(Workout(exercises = listOf(exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, hour = 9, minute = 0))))

        // when
        testScheduler.advanceUntilIdle()
        component.onPrimaryCtaClick()

        // then
        assertThat(component.model.value.ctaState).isEqualTo(MainCtaState.DayClosing)
        assertThat(componentOutput.any { it is ComponentOutput.Trainings.OpenExerciseBlock }).isFalse()
    }

    @Test
    fun `when perfect day completed then CTA reports perfect day and does not navigate`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 23, minute = 0)
        calendarFlow.value = listOf(Workout(exercises = perfectDayExercises()))

        // when
        testScheduler.advanceUntilIdle()
        component.onPrimaryCtaClick()

        // then
        assertThat(component.model.value.ctaState).isEqualTo(MainCtaState.PerfectDay)
        assertThat(component.model.value.dailyProgressPercent).isEqualTo(100)
        assertThat(componentOutput.any { it is ComponentOutput.Trainings.OpenExerciseBlock }).isFalse()
    }

    @Test
    fun `when workouts are consecutive then tree growth streak is calculated`() = runTest(testScheduler) {
        // given
        timeUtils.current = instantAt(hour = 12, minute = 0)
        calendarFlow.value = listOf(
            Workout(exercises = listOf(exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, day = 13, hour = 9, minute = 0))),
            Workout(exercises = listOf(exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, day = 14, hour = 9, minute = 0))),
            Workout(exercises = listOf(exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, day = 15, hour = 9, minute = 0))),
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.treeGrowthStreakDays).isEqualTo(3)
    }

    @Test
    fun `when highlight provider fails then component publishes error output`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("highlight failed")
        highlightsProvider.exception = exception
        val testComponent = createComponent()

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(testComponent).isInstanceOf(MainTabComponent::class)
        assertThat(
            componentOutput.filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable.message == exception.message }
        ).isTrue()
    }

    override fun createComponent(): MainTabComponent =
        MainTabComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            settings = settings,
            timeUtils = timeUtils,
            calendarWatcher = object : BlinklyCalendarWatcher {
                override val calendar: Flow<List<Workout>> = calendarFlow
            },
            highlightsProvider = highlightsProvider,
            treeProgressWatcher = object : BlinklyTreeProgressWatcher {
                override val tree: Flow<Tree> = treeFlow
                override val garden: Flow<TreeGarden> = flowOf(emptyGarden())
            },
            mainTabOutput = { componentOutput.add(it) },
        )

    private fun perfectDayExercises(): List<Exercise> =
        listOf(
            exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, hour = 8, minute = 0),
            exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS, hour = 8, minute = 5),
            exercise(ExerciseBlock.A, ExerciseType.DIAGONAL_GAZES, hour = 8, minute = 10),
            exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT, hour = 18, minute = 0),
            exercise(ExerciseBlock.B, ExerciseType.CLOCK_ROLLS, hour = 18, minute = 5),
            exercise(ExerciseBlock.B, ExerciseType.PALMING, hour = 18, minute = 10),
            exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, hour = 11, minute = 0),
            exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, hour = 15, minute = 0),
        )

    private fun exercise(
        block: ExerciseBlock,
        type: ExerciseType,
        day: Int = 15,
        hour: Int,
        minute: Int,
        timeZone: TimeZone = TimeZone.UTC,
    ): Exercise =
        Exercise(
            block = block,
            type = type,
            completedAt = instantAt(day = day, hour = hour, minute = minute, timeZone = timeZone),
        )

    private fun instantAt(day: Int = 15, hour: Int, minute: Int, timeZone: TimeZone = TimeZone.UTC): Instant =
        LocalDateTime(year = 2026, month = 3, day = day, hour = hour, minute = minute).toInstant(timeZone)

    private class FakeTimeUtils : BlinklyTimeUtils {
        var current: Instant = LocalDateTime(year = 2026, month = 3, day = 15, hour = 12, minute = 0).toInstant(TimeZone.UTC)
        var zone: TimeZone = TimeZone.UTC

        override fun now(): Instant = current
        override fun timeZone(): TimeZone = zone
    }

    private class FakeHighlightsProvider : BlinklyHighlightsProvider {
        var exception: Throwable? = null

        override suspend fun get(): HighlightOfTheDay =
            exception?.let { throw it } ?: HighlightOfTheDay.Tip(7)

        override suspend fun forceNextHighlight(): HighlightOfTheDay = HighlightOfTheDay.Tip(8)
        override suspend fun reset() = Unit
        override suspend fun getShownCount(): Int = 1
    }

    private class FakeSettings : BlinklySettings {
        override var blinkBreakCount: Int = 60
        override var nearFarFocusCount: Int = 10
        override var nearFarFocusDuration: Float = 5f
        override var diagonalGazesCount: Int = 5
        override var diagonalGazesDuration: Float = 3f
        override var figureEightCount: Int = 10
        override var clockRollsEachSide: Int = 5
        override var palmingDuration: Int = 120
        override var themeState: ThemeState = ThemeState.SYSTEM
        override var lightThemeWorkoutIndex: Int = 0
        override var darkThemeWorkoutIndex: Int = 0
        override var lastTreeProgressCheckDate: LocalDate? = null
        override var displayedHighlights: List<Int> = emptyList()
        override var currentHighlightDate: LocalDate? = null
        override var onboardingDisplayed: Boolean = false
    }

    private companion object {
        val DEFAULT_TREE: Tree = Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f)

        fun emptyGarden(): TreeGarden =
            TreeGarden(
                currentTree = DEFAULT_TREE,
                grownTrees = emptyList(),
                totalTrees = TreeType.entries.size,
                nextTreeType = TreeType.FRAXINUS_EXCELSIOR,
                daysToNextTree = 28,
            )
    }
}
