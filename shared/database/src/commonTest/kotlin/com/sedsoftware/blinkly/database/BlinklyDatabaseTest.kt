package com.sedsoftware.blinkly.database

import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
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
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
        assertThat(calendar).isEmpty()
    }

    @Test
    fun `when no achievements saved then return empty list`() = runTest(testScheduler) {
        // given
        // when
        val achievements = database.currentAchievements().first()
        // then
        assertThat(achievements).isEmpty()
    }

    @Test
    fun `when no reminders saved then return empty list`() = runTest(testScheduler) {
        // given
        // when
        val reminders = database.currentReminders().first()
        // then
        assertThat(reminders).isEmpty()
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

    @Test
    fun `when reminder saved then reminders subscription updated`() = runTest(testScheduler) {
        // given
        val now = Clock.System.now()
        val reminder = Reminder(
            uuid = "test",
            date = now.toLocalDateTime(TimeZone.UTC),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.DAILY,
            weekDays = listOf(DayOfWeek.MONDAY)
        )
        var reminders: List<Reminder>? = null

        // when
        val collectJob = launch { database.currentReminders().collect { reminders = it } }
        database.saveReminder(reminder)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminders).isNotNull()
        assertThat(reminders!!).isNotEmpty()
        assertThat(reminders.first()).isEqualTo(reminder)
        collectJob.cancel()
    }

    @Test
    fun `when reminders saved then reminders subscription updated`() = runTest(testScheduler) {
        // given
        val now = Clock.System.now()
        val uuid1 = "test1"
        val uuid2 = "test2"
        val reminder1 = Reminder(
            uuid = uuid1,
            date = now.toLocalDateTime(TimeZone.UTC),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.DAILY,
            weekDays = listOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )
        val reminder2 = Reminder(
            uuid = uuid2,
            date = now.toLocalDateTime(TimeZone.UTC),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.WEEKLY,
            weekDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
        )
        var reminders: List<Reminder>? = null

        // when
        val collectJob = launch { database.currentReminders().collect { reminders = it } }
        database.saveReminders(listOf(reminder1, reminder2))
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminders).isNotNull()
        assertThat(reminders!!).isNotEmpty()
        assertThat(reminders.size).isEqualTo(2)
        collectJob.cancel()
    }

    @Test
    fun `when reminder deleted then subscription updated`() = runTest(testScheduler) {
        val now = Clock.System.now()
        val uuid1 = "test1"
        val uuid2 = "test2"
        val reminder1 = Reminder(
            uuid = uuid1,
            date = now.toLocalDateTime(TimeZone.UTC),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.DAILY,
            weekDays = listOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )
        val reminder2 = Reminder(
            uuid = uuid2,
            date = now.toLocalDateTime(TimeZone.UTC),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.WEEKLY,
            weekDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
        )

        var reminders: List<Reminder>? = null

        // when
        val collectJob = launch { database.currentReminders().collect { reminders = it } }
        database.saveReminder(reminder1)
        testScheduler.advanceUntilIdle()
        database.saveReminder(reminder2)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminders).isNotNull()
        assertThat(reminders!!).isNotEmpty()
        assertThat(reminders.size).isEqualTo(2)
        assertThat(reminders.first()).isEqualTo(reminder1)
        assertThat(reminders.last()).isEqualTo(reminder2)

        // when
        database.deleteReminder(uuid1)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminders).isNotEmpty()
        assertThat(reminders.size).isEqualTo(1)
        assertThat(reminders.first()).isEqualTo(reminder2)

        collectJob.cancel()
    }

    @Test
    fun `when all reminders deleted then subscription updated`() = runTest(testScheduler) {
        val now = Clock.System.now()
        val uuid1 = "test1"
        val uuid2 = "test2"
        val reminder1 = Reminder(
            uuid = uuid1,
            date = now.toLocalDateTime(TimeZone.UTC),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.DAILY,
            weekDays = listOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )
        val reminder2 = Reminder(
            uuid = uuid2,
            date = now.toLocalDateTime(TimeZone.UTC),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.WEEKLY,
            weekDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
        )

        var reminders: List<Reminder>? = null

        // when
        val collectJob = launch { database.currentReminders().collect { reminders = it } }
        database.saveReminder(reminder1)
        testScheduler.advanceUntilIdle()
        database.saveReminder(reminder2)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminders).isNotNull()
        assertThat(reminders!!).isNotEmpty()
        assertThat(reminders.size).isEqualTo(2)
        assertThat(reminders.first()).isEqualTo(reminder1)
        assertThat(reminders.last()).isEqualTo(reminder2)

        // when
        database.deleteReminders()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminders).isEmpty()
        collectJob.cancel()
    }
}
