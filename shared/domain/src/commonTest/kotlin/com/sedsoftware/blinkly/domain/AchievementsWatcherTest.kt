package com.sedsoftware.blinkly.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.fakes.FakeData
import com.sedsoftware.blinkly.domain.fakes.FakeSettings
import com.sedsoftware.blinkly.domain.impl.AchievementsWatcherImpl
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.domain.model.Workout
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AchievementsWatcherTest : BaseDomainTest() {
    private val achievementsFlow: MutableStateFlow<List<Achievement>> = MutableStateFlow(emptyList())
    private val calendarFlow: MutableStateFlow<List<Workout>> = MutableStateFlow(emptyList())

    private val database: BlinklyDatabase = mock {
        everySuspend { currentCalendar() } returns calendarFlow
        everySuspend { saveExercise(any()) } returns Unit
        everySuspend { currentAchievements() } returns achievementsFlow
        everySuspend { unlockAchievement(any()) } returns Unit
    }

    private val fakeSettings: FakeSettings = FakeSettings()

    private val watcher: AchievementsWatcher = AchievementsWatcherImpl(
        database, notifier, fakeSettings, timeUtils, testDispatchers
    )

    @Test
    fun `any exercise at the first day should unlock FirstSpark`() = runTest(testScheduler) {
        // given
        val today = now
        val workout = FakeData.getSingleExerciseWorkout(today)
        val calendar: List<Workout> = listOf(workout)
        every { timeUtils.now() } returns today
        val unlockedAchievement = Achievement(
            AchievementType.FIRST_SPARK,
            AchievementLevel.BEGINNER,
            today,
        )

        // when
        val collectJob = launch { watcher.achievements.collect {} }

        achievementsFlow.emit(emptyList())
        calendarFlow.emit(calendar)
        testScheduler.advanceUntilIdle()

        // then
        verifySuspend { database.unlockAchievement(unlockedAchievement) }
        verifySuspend { notifier.achievementUnlocked(AchievementType.FIRST_SPARK) }

        collectJob.cancel()
    }

    @Test
    fun `any exercise in light theme should write light theme index`() = runTest(testScheduler) {
        // given
        val today = now
        val workout = FakeData.getSingleExerciseWorkout(today)
        val calendar: List<Workout> = listOf(workout)

        fakeSettings.lightThemeWorkoutIndex = 0
        fakeSettings.darkThemeWorkoutIndex = 0
        fakeSettings.themeState = ThemeState.LIGHT

        // when
        val collectJob = launch { watcher.achievements.collect {} }

        achievementsFlow.emit(emptyList())
        calendarFlow.emit(calendar)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(fakeSettings.lightThemeWorkoutIndex).isEqualTo(1)

        fakeSettings.lightThemeWorkoutIndex = 0
        fakeSettings.darkThemeWorkoutIndex = 0
        fakeSettings.themeState = ThemeState.SYSTEM
        collectJob.cancel()
    }

    @Test
    fun `any exercise in dark theme should write dark theme index`() = runTest(testScheduler) {
        // given
        val today = now
        val workout = FakeData.getSingleExerciseWorkout(today)
        val calendar: List<Workout> = listOf(workout)

        fakeSettings.lightThemeWorkoutIndex = 0
        fakeSettings.darkThemeWorkoutIndex = 0
        fakeSettings.themeState = ThemeState.DARK

        // when
        val collectJob = launch { watcher.achievements.collect {} }

        achievementsFlow.emit(emptyList())
        calendarFlow.emit(calendar)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(fakeSettings.darkThemeWorkoutIndex).isEqualTo(1)

        fakeSettings.lightThemeWorkoutIndex = 0
        fakeSettings.darkThemeWorkoutIndex = 0
        fakeSettings.themeState = ThemeState.SYSTEM
        collectJob.cancel()
    }

    @Test
    fun `any exercise when both light and dark workouts completed should unlock Yin Yang`() = runTest(testScheduler) {
        // given
        val today = now
        val workout = FakeData.getSingleExerciseWorkout(today)
        val calendar: List<Workout> = listOf(workout)
        every { timeUtils.now() } returns today
        val unlockedAchievement = Achievement(
            type = AchievementType.YIN_YANG,
            level = AchievementLevel.HIDDEN,
            unlockedAt = today,
        )

        fakeSettings.lightThemeWorkoutIndex = 1
        fakeSettings.darkThemeWorkoutIndex = 2

        // when
        val collectJob = launch { watcher.achievements.collect {} }

        achievementsFlow.emit(emptyList())
        calendarFlow.emit(calendar)
        testScheduler.advanceUntilIdle()

        // then
        verifySuspend { database.unlockAchievement(unlockedAchievement) }
        verifySuspend { notifier.achievementUnlocked(AchievementType.YIN_YANG) }

        fakeSettings.lightThemeWorkoutIndex = 0
        fakeSettings.darkThemeWorkoutIndex = 0
        collectJob.cancel()
    }
}
