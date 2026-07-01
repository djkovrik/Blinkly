package com.sedsoftware.blinkly.component.progress

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.progress.integration.ProgressTabComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyAchievementsWatcher
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeGarden
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.time.Instant

class ProgressTabComponentTest : ComponentTest<ProgressTabComponent>() {

    private val calendarFlow: MutableStateFlow<List<Workout>> = MutableStateFlow(emptyList())
    private val achievementsFlow: MutableStateFlow<List<Achievement>> = MutableStateFlow(emptyList())
    private val treeFlow: MutableStateFlow<Tree> = MutableStateFlow(DEFAULT_TREE)
    private val timeUtils: FakeTimeUtils = FakeTimeUtils()

    @Test
    fun `when component created then model contains calendar tree and recent achievements`() = runTest(testScheduler) {
        // given
        treeFlow.value = Tree(TreeStage.MATURE, TreeType.QUERCUS_ROBUR, 82f)
        calendarFlow.value = listOf(
            Workout(
                exercises = perfectDayExercises(day = 6) + listOf(
                    exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, day = 7),
                    exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, day = 18),
                )
            )
        )
        achievementsFlow.value = listOf(
            achievement(AchievementType.FIRST_SPARK, "2026-03-01T08:00:00Z"),
            achievement(AchievementType.BLINK_MASTER, "2026-03-18T08:00:00Z"),
            achievement(AchievementType.DIAMOND_EYES, "2026-03-16T08:00:00Z"),
            achievement(AchievementType.FAR_SIGHTED, "2026-03-20T08:00:00Z"),
            Achievement(AchievementType.EAGLE_EYE, AchievementLevel.PRO, unlockedAt = null),
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        val model = component.model.value
        assertThat(model.tree).isEqualTo(Tree(TreeStage.MATURE, TreeType.QUERCUS_ROBUR, 82f))
        assertThat(model.calendarWeeks.flatten().filterNotNull().first { it.date.day == 6 }.state)
            .isEqualTo(ProgressTabComponent.CalendarDayState.PERFECT)
        assertThat(model.calendarWeeks.flatten().filterNotNull().first { it.date.day == 7 }.state)
            .isEqualTo(ProgressTabComponent.CalendarDayState.WORKOUT)
        assertThat(model.calendarWeeks.flatten().filterNotNull().first { it.date.day == 8 }.state)
            .isEqualTo(ProgressTabComponent.CalendarDayState.EMPTY)
        assertThat(model.recentAchievements.map { it?.type }).isEqualTo(
            listOf(AchievementType.FAR_SIGHTED, AchievementType.BLINK_MASTER, AchievementType.DIAMOND_EYES)
        )
    }

    @Test
    fun `when no achievements are unlocked then model contains placeholders`() = runTest(testScheduler) {
        // given
        achievementsFlow.value = listOf(
            Achievement(AchievementType.FIRST_SPARK, AchievementLevel.BEGINNER, unlockedAt = null),
            Achievement(AchievementType.BLINK_MASTER, AchievementLevel.INTERMEDIATE, unlockedAt = null),
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.recentAchievements.size).isEqualTo(3)
        component.model.value.recentAchievements.forEach { achievement ->
            assertThat(achievement).isNull()
        }
    }

    @Test
    fun `when achievements clicked then output opens achievements`() = runTest(testScheduler) {
        // when
        component.onAchievementsClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Progress.OpenAchievements)
    }

    @Test
    fun `when garden clicked then output opens garden`() = runTest(testScheduler) {
        // when
        component.onGardenClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Progress.OpenGarden)
    }

    @Test
    fun `when watcher fails then component publishes error output`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("calendar failed")
        val testComponent = createComponent(calendar = flow { throw exception })

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(testComponent).isInstanceOf(ProgressTabComponent::class)
        assertThat(
            componentOutput.filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable.message == exception.message }
        ).isTrue()
    }

    override fun createComponent(): ProgressTabComponent =
        createComponent(calendar = calendarFlow)

    private fun createComponent(calendar: Flow<List<Workout>>): ProgressTabComponent =
        ProgressTabComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            timeUtils = timeUtils,
            calendarWatcher = object : BlinklyCalendarWatcher {
                override val calendar: Flow<List<Workout>> = calendar
            },
            achievementsWatcher = object : BlinklyAchievementsWatcher {
                override val achievements: Flow<List<Achievement>> = achievementsFlow
            },
            treeProgressWatcher = object : BlinklyTreeProgressWatcher {
                override val tree: Flow<Tree> = treeFlow
                override val garden: Flow<TreeGarden> = flowOf(emptyGarden())
            },
            progressTabOutput = { componentOutput.add(it) },
        )

    private fun perfectDayExercises(day: Int): List<Exercise> =
        listOf(
            exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, day = day),
            exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS, day = day),
            exercise(ExerciseBlock.A, ExerciseType.DIAGONAL_GAZES, day = day),
            exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT, day = day),
            exercise(ExerciseBlock.B, ExerciseType.CLOCK_ROLLS, day = day),
            exercise(ExerciseBlock.B, ExerciseType.PALMING, day = day),
            exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, day = day, hour = 11),
            exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3, day = day, hour = 15),
        )

    private fun exercise(
        block: ExerciseBlock,
        type: ExerciseType,
        day: Int,
        hour: Int = 9,
        minute: Int = 0,
    ): Exercise =
        Exercise(
            block = block,
            type = type,
            completedAt = instantAt(day = day, hour = hour, minute = minute),
        )

    private fun achievement(type: AchievementType, unlockedAt: String): Achievement =
        Achievement(
            type = type,
            level = AchievementLevel.INTERMEDIATE,
            unlockedAt = Instant.parse(unlockedAt),
        )

    private fun instantAt(day: Int = 15, hour: Int, minute: Int): Instant =
        LocalDateTime(year = 2026, month = 3, day = day, hour = hour, minute = minute).toInstant(TimeZone.UTC)

    private class FakeTimeUtils : BlinklyTimeUtils {
        override fun now(): Instant = LocalDateTime(year = 2026, month = 3, day = 15, hour = 12, minute = 0).toInstant(TimeZone.UTC)
        override fun timeZone(): TimeZone = TimeZone.UTC
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
