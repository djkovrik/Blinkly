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
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.domain.model.Workout
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalTime
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

    private val timeUtils: BlinklyTimeUtils = mock {
        every { now() } returns Clock.System.now()
        every { timeZone() } returns TimeZone.UTC
    }

    lateinit var driver: SqlDriver
    lateinit var database: BlinklyDatabase

    @BeforeTest
    fun setup() {
        driver = TestDriverFactory()
        database = BlinklyDatabaseImpl(dispatchers = testDispatchers, driver = driver, timeUtils = timeUtils)
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
    fun `schedule configurations and physical alarms round trip`() = runTest(testScheduler) {
        val schedules = listOf(
            ReminderSchedule("daily", ReminderType.TWENTY_X3, ReminderScheduleConfiguration.Daily(LocalTime(10, 0))),
            ReminderSchedule(
                "weekly",
                ReminderType.TWENTY_X3,
                ReminderScheduleConfiguration.WeeklySingle(LocalTime(14, 30), DayOfWeek.WEDNESDAY),
            ),
            ReminderSchedule(
                "period",
                ReminderType.TWENTY_X3,
                ReminderScheduleConfiguration.WorkdayPeriod(
                    LocalTime(9, 0),
                    LocalTime(18, 0),
                    20,
                    listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                ),
            ),
        )
        val reminders = schedules.mapIndexed { index, schedule -> reminder("alarm-$index", schedule.id) }

        schedules.zip(reminders).forEach { (schedule, reminder) ->
            database.saveReminderSchedule(schedule, listOf(reminder))
        }

        assertThat(database.currentReminderSchedules().first()).isEqualTo(schedules)
        assertThat(database.currentReminders().first()).isEqualTo(reminders)
        assertThat(database.remindersBySchedule("period")).isEqualTo(listOf(reminders.last()))
    }

    @Test
    fun `deleting schedule removes only its parent and children`() = runTest(testScheduler) {
        val first = ReminderSchedule("first", ReminderType.TWENTY_X3, ReminderScheduleConfiguration.Daily(LocalTime(10, 0)))
        val second = ReminderSchedule("second", ReminderType.TWENTY_X3, ReminderScheduleConfiguration.Daily(LocalTime(11, 0)))
        val firstAlarm = reminder("first-alarm", first.id)
        val secondAlarm = reminder("second-alarm", second.id)
        database.saveReminderSchedule(first, listOf(firstAlarm))
        database.saveReminderSchedule(second, listOf(secondAlarm))

        database.deleteReminderSchedule(first.id)

        assertThat(database.currentReminderSchedules().first()).isEqualTo(listOf(second))
        assertThat(database.currentReminders().first()).isEqualTo(listOf(secondAlarm))
    }

    @Test
    fun `snapshot includes and replaces schedules with alarms`() = runTest(testScheduler) {
        val schedule = ReminderSchedule(
            "period",
            ReminderType.TWENTY_X3,
            ReminderScheduleConfiguration.WorkdayPeriod(
                LocalTime(9, 0),
                LocalTime(18, 0),
                20,
                listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            ),
        )
        val alarms = listOf(reminder("one", schedule.id), reminder("two", schedule.id))
        val snapshot = BlinklyDatabaseSnapshot(emptyList(), emptyList(), listOf(schedule), alarms)

        database.replaceSnapshot(snapshot)

        assertThat(database.currentSnapshot()).isEqualTo(snapshot)
    }

    @Test
    fun `delete reminders clears schedules and alarms`() = runTest(testScheduler) {
        val schedule = ReminderSchedule("daily", ReminderType.TWENTY_X3, ReminderScheduleConfiguration.Daily(LocalTime(10, 0)))
        database.saveReminderSchedule(schedule, listOf(reminder("alarm", schedule.id)))

        database.deleteReminders()

        assertThat(database.currentReminderSchedules().first()).isEmpty()
        assertThat(database.currentReminders().first()).isEmpty()
    }

    private fun reminder(uuid: String, scheduleId: String): Reminder =
        Reminder(
            uuid = uuid,
            scheduleId = scheduleId,
            date = Clock.System.now().toLocalDateTime(TimeZone.UTC),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.WEEKLY,
        )
}
