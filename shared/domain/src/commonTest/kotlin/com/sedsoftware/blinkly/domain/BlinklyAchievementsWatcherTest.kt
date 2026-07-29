package com.sedsoftware.blinkly.domain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.fakes.FakeData
import com.sedsoftware.blinkly.domain.impl.BlinklyAchievementsWatcherImpl
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
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class BlinklyAchievementsWatcherTest : BaseDomainTest() {

    private val achievementsFlow: MutableStateFlow<List<Achievement>> = MutableStateFlow(emptyList())
    private val calendarFlow: MutableStateFlow<List<Workout>> = MutableStateFlow(emptyList())

    private val database: BlinklyDatabase = mock {
        everySuspend { currentCalendar() } returns calendarFlow
        everySuspend { saveExercise(any()) } returns Unit
        everySuspend { currentAchievements() } returns achievementsFlow
        everySuspend { unlockAchievement(any()) } returns true
    }

    private val watcher: BlinklyAchievementsWatcher = BlinklyAchievementsWatcherImpl(
        database, notifier, settings, timeUtils, testDispatchers
    )

    @Test
    fun `when any exercise at the first day then should unlock FirstSpark`() = runTest(testScheduler) {
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
        testScheduler.advanceUntilIdle()

        calendarFlow.emit(calendar)
        testScheduler.advanceUntilIdle()

        // then
        verifySuspend { database.unlockAchievement(unlockedAchievement) }
        verifySuspend { notifier.achievementUnlocked(AchievementType.FIRST_SPARK) }

        collectJob.cancel()
    }

    @Test
    fun `when any exercise in light theme then should write light theme index`() = runTest(testScheduler) {
        // given
        val today = now
        val workout = FakeData.getSingleExerciseWorkout(today)
        val calendar: List<Workout> = listOf(workout)

        settings.lightThemeWorkoutIndex = 0
        settings.darkThemeWorkoutIndex = 0
        settings.themeState = ThemeState.LIGHT

        // when
        val collectJob = launch { watcher.achievements.collect {} }

        achievementsFlow.emit(emptyList())
        calendarFlow.emit(calendar)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(settings.lightThemeWorkoutIndex).isEqualTo(1)

        settings.lightThemeWorkoutIndex = 0
        settings.darkThemeWorkoutIndex = 0
        settings.themeState = ThemeState.SYSTEM
        collectJob.cancel()
    }

    @Test
    fun `when any exercise in dark theme then should write dark theme index`() = runTest(testScheduler) {
        // given
        val today = now
        val workout = FakeData.getSingleExerciseWorkout(today)
        val calendar: List<Workout> = listOf(workout)

        settings.lightThemeWorkoutIndex = 0
        settings.darkThemeWorkoutIndex = 0
        settings.themeState = ThemeState.DARK

        // when
        val collectJob = launch { watcher.achievements.collect {} }

        achievementsFlow.emit(emptyList())
        calendarFlow.emit(calendar)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(settings.darkThemeWorkoutIndex).isEqualTo(1)

        settings.lightThemeWorkoutIndex = 0
        settings.darkThemeWorkoutIndex = 0
        settings.themeState = ThemeState.SYSTEM
        collectJob.cancel()
    }

    @Test
    fun `when any exercise when both light and dark workouts completed then should unlock Yin Yang`() = runTest(testScheduler) {
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

        settings.lightThemeWorkoutIndex = 1
        settings.darkThemeWorkoutIndex = 1

        // when
        val collectJob = launch { watcher.achievements.collect {} }

        achievementsFlow.emit(emptyList())
        calendarFlow.emit(calendar)
        testScheduler.advanceUntilIdle()

        // then
        verifySuspend { database.unlockAchievement(unlockedAchievement) }
        verifySuspend { notifier.achievementUnlocked(AchievementType.YIN_YANG) }

        settings.lightThemeWorkoutIndex = 0
        settings.darkThemeWorkoutIndex = 0
        collectJob.cancel()
    }

    @Test
    fun `when exercises completed in system light and dark themes on same day then should unlock Yin Yang`() =
        runTest(testScheduler) {
            // given
            val firstWorkout = FakeData.getSingleExerciseWorkout(now)
            val secondWorkout = Workout(FakeData.getWorkoutDayFull(now).exercises.take(2))
            settings.themeState = ThemeState.SYSTEM
            settings.lightThemeWorkoutIndex = 0
            settings.darkThemeWorkoutIndex = 0
            watcher.onThemeResolved(isDark = false)

            val collectJob = launch { watcher.achievements.collect {} }
            calendarFlow.emit(listOf(firstWorkout))
            testScheduler.advanceUntilIdle()

            assertThat(settings.lightThemeWorkoutIndex).isEqualTo(1)

            // when
            watcher.onThemeResolved(isDark = true)
            calendarFlow.emit(listOf(secondWorkout))
            testScheduler.advanceUntilIdle()

            // then
            assertThat(settings.darkThemeWorkoutIndex).isEqualTo(2)
            verifySuspend { notifier.achievementUnlocked(AchievementType.YIN_YANG) }

            collectJob.cancel()
        }

    @Test
    fun `when achievement unlocked then should update resulting flow`() = runTest(testScheduler) {
        // given
        val achievement = Achievement(AchievementType.FIRST_SPARK, AchievementLevel.BEGINNER, now)
        var achievements: List<Achievement> = emptyList()
        // when
        val collectJob = launch { watcher.achievements.collect { achievements = it } }
        achievementsFlow.emit(listOf(achievement))
        testScheduler.advanceUntilIdle()

        // then
        assertThat(achievements.size).isEqualTo(AchievementType.entries.size)
        assertThat(achievements).contains(achievement)
        collectJob.cancel()
    }

    @Test
    fun `when achievement already persisted then should not unlock or notify again`() = runTest(testScheduler) {
        // given
        val achievement = Achievement(AchievementType.FIRST_SPARK, AchievementLevel.BEGINNER, now)
        achievementsFlow.value = listOf(achievement)
        calendarFlow.value = listOf(FakeData.getSingleExerciseWorkout(now))

        // when
        val collectJob = launch { watcher.achievements.collect {} }
        testScheduler.advanceUntilIdle()

        // then
        verifySuspend(exactly(0)) { database.unlockAchievement(any()) }
        verifySuspend(exactly(0)) { notifier.achievementUnlocked(any()) }
        collectJob.cancel()
    }

    @Test
    fun `when persistence rejects duplicate unlock then should not notify`() = runTest(testScheduler) {
        // given
        everySuspend { database.unlockAchievement(any()) } returns false
        calendarFlow.value = listOf(FakeData.getSingleExerciseWorkout(now))

        // when
        val collectJob = launch { watcher.achievements.collect {} }
        testScheduler.advanceUntilIdle()

        // then
        verifySuspend(exactly(1)) { database.unlockAchievement(any()) }
        verifySuspend(exactly(0)) { notifier.achievementUnlocked(any()) }
        collectJob.cancel()
    }
}
