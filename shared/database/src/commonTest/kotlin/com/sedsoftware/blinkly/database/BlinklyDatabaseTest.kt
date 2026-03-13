package com.sedsoftware.blinkly.database

import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import com.sedsoftware.blinkly.database.impl.BlinklyDatabaseImpl
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock

class BlinklyDatabaseTest {

    private val testScheduler: TestCoroutineScheduler = TestCoroutineScheduler()

    private val testDispatchers: BlinklyDispatchers =
        object : BlinklyDispatchers {
            override val main: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
            override val io: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
        }


    lateinit var driver: SqlDriver
    lateinit var database: BlinklyDatabase

    @BeforeTest
    fun setup() {
        driver = TestDriverFactory()
        database = BlinklyDatabaseImpl(testDispatchers, driver)
    }

    @Test
    fun `when no exercises saved then return empty list`() = runTest(testScheduler) {
        // given
        // when
        val calendar = database.currentCalendar().first()
        // then
        assertThat(calendar.isEmpty())
    }

    @Test
    fun `when no achievements saved then return empty list`() = runTest(testScheduler) {
        // given
        // when
        val achievements = database.currentAchievements().first()
        // then
        assertThat(achievements.isEmpty())
    }

    @Test
    fun `when exercise saved then calendar subscription updated`() = runTest(testScheduler) {
        // given
        val now = Clock.System.now()
        val exercise = Exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, now)
        var workouts: List<Workout>? = null

        // when
        val collectJob = launch { database.currentCalendar().collect { workouts = it } }
        database.saveExercise(exercise)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(workouts).isNotNull()
        assertThat(workouts!!).isNotEmpty()
        assertThat(workouts.first().exercises).contains(exercise)
        collectJob.cancel()
    }

    @Test
    fun `when achievement saved then achievements subscription updated`() = runTest(testScheduler) {
        // given
        val now = Clock.System.now()
        val achievement = Achievement(AchievementType.BLINK_MASTER, AchievementLevel.BEGINNER, now)
        var achievements: List<Achievement>? = null

        // when
        val collectJob = launch { database.currentAchievements().collect { achievements = it } }
        database.unlockAchievement(achievement)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(achievements).isNotNull()
        assertThat(achievements!!).isNotEmpty()
        assertThat(achievements.first()).isEqualTo(achievement)
        collectJob.cancel()
    }
}
