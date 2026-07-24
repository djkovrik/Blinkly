package com.sedsoftware.blinkly.component.sync.domain

import com.sedsoftware.blinkly.domain.external.BlinklyAuthService
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyRemoteSyncDataSource
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.BlinklySettingsSnapshot
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.RemoteBlinklySnapshot
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class BlinklySyncManagerImplTest {

    private val user: BlinklyUser = BlinklyUser(id = "user-id", displayName = "User", email = "user@example.com")
    private val authService: FakeBlinklyAuthService = FakeBlinklyAuthService(user)
    private val database: FakeBlinklyDatabase = FakeBlinklyDatabase()
    private val settings: FakeBlinklySettings = FakeBlinklySettings()
    private val remoteDataSource: FakeRemoteSyncDataSource = FakeRemoteSyncDataSource()
    private val timeUtils: FakeTimeUtils = FakeTimeUtils(now = instant(50))
    private var rescheduleCount: Int = 0

    @Test
    fun `when remote snapshot is missing then local snapshot is uploaded`() = runTest {
        // given
        val localDatabaseChangedAt = instant(10)
        val localSettingsChangedAt = instant(12)
        val localSnapshot = databaseSnapshot(exercises = listOf(exercise(10)))
        settings.lastLocalDatabaseChangeAt = localDatabaseChangedAt
        settings.lastLocalSettingsChangeAt = localSettingsChangedAt
        database.snapshot = localSnapshot
        settings.blinkBreakCount = 90
        remoteDataSource.remote = null

        // when
        createManager(backgroundScope).syncNow()

        // then
        assertEquals(localSnapshot, remoteDataSource.writtenSnapshot?.database)
        assertEquals(settingsSnapshot(blinkBreakCount = 90), remoteDataSource.writtenSnapshot?.settings)
        assertEquals(localDatabaseChangedAt, remoteDataSource.writtenSnapshot?.databaseUpdatedAt)
        assertEquals(localSettingsChangedAt, remoteDataSource.writtenSnapshot?.settingsUpdatedAt)
        assertEquals(timeUtils.now(), remoteDataSource.writtenSnapshot?.updatedAt)
        assertEquals(timeUtils.now(), settings.lastSyncedAt)
        assertEquals(timeUtils.now(), settings.lastRemoteUpdatedAt)
        assertNull(database.replacedSnapshot)
    }

    @Test
    fun `when only local database changed then local database is uploaded without overwriting remote settings`() = runTest {
        // given
        val baseline = instant(20)
        val localDatabaseChangedAt = instant(30)
        val localSnapshot = databaseSnapshot(exercises = listOf(exercise(30)))
        val remoteSettings = settingsSnapshot(blinkBreakCount = 75)
        settings.lastRemoteUpdatedAt = baseline
        settings.lastLocalDatabaseChangeAt = localDatabaseChangedAt
        database.snapshot = localSnapshot
        remoteDataSource.remote = remoteSnapshot(
            updatedAt = baseline,
            databaseUpdatedAt = baseline,
            settingsUpdatedAt = baseline,
            database = databaseSnapshot(exercises = listOf(exercise(10))),
            settings = remoteSettings,
        )

        // when
        createManager(backgroundScope).syncNow()

        // then
        assertEquals(localSnapshot, remoteDataSource.writtenSnapshot?.database)
        assertEquals(remoteSettings, remoteDataSource.writtenSnapshot?.settings)
        assertEquals(localDatabaseChangedAt, remoteDataSource.writtenSnapshot?.databaseUpdatedAt)
        assertEquals(baseline, remoteDataSource.writtenSnapshot?.settingsUpdatedAt)
        assertEquals(75, settings.blinkBreakCount)
    }

    @Test
    fun `when only remote database changed then remote database is applied`() = runTest {
        // given
        val baseline = instant(20)
        val remoteDatabaseUpdatedAt = instant(30)
        val remoteDatabase = databaseSnapshot(exercises = listOf(exercise(30)))
        settings.lastRemoteUpdatedAt = baseline
        remoteDataSource.remote = remoteSnapshot(
            updatedAt = remoteDatabaseUpdatedAt,
            databaseUpdatedAt = remoteDatabaseUpdatedAt,
            settingsUpdatedAt = baseline,
            database = remoteDatabase,
        )

        // when
        createManager(backgroundScope).syncNow()

        // then
        assertEquals(remoteDatabase, database.replacedSnapshot)
        assertEquals(timeUtils.now(), settings.lastSyncedAt)
        assertEquals(remoteDatabaseUpdatedAt, settings.lastRemoteUpdatedAt)
        assertNull(remoteDataSource.writtenSnapshot)
        assertEquals(0, rescheduleCount)
    }

    @Test
    fun `when remote reminders changed then physical alarms are rescheduled`() = runTest {
        val baseline = instant(20)
        val remoteReminder = reminder(uuid = "remote", minute = 20)
        val remoteDatabase = databaseSnapshot(reminders = listOf(remoteReminder))
        settings.lastRemoteUpdatedAt = baseline
        remoteDataSource.remote = remoteSnapshot(
            updatedAt = instant(30),
            databaseUpdatedAt = instant(30),
            settingsUpdatedAt = baseline,
            database = remoteDatabase,
        )

        createManager(backgroundScope).syncNow()

        assertEquals(remoteDatabase, database.replacedSnapshot)
        assertEquals(1, rescheduleCount)
    }

    @Test
    fun `when local and remote database changed then snapshots are merged and uploaded`() = runTest {
        // given
        val baseline = instant(20)
        val localExercise = exercise(30, ExerciseBlock.A)
        val remoteExercise = exercise(31, ExerciseBlock.B)
        val duplicateExercise = exercise(32, ExerciseBlock.C)
        val localAchievement = achievement(AchievementType.FIRST_SPARK, unlockedAt = instant(32))
        val remoteAchievement = achievement(AchievementType.FIRST_SPARK, unlockedAt = instant(30))
        val localReminder = reminder(uuid = "same-id", minute = 1)
        val remoteReminder = reminder(uuid = "same-id", minute = 2)
        val localDatabase = databaseSnapshot(
            exercises = listOf(localExercise, duplicateExercise),
            achievements = listOf(localAchievement),
            reminders = listOf(localReminder),
        )
        val remoteDatabase = databaseSnapshot(
            exercises = listOf(remoteExercise, duplicateExercise),
            achievements = listOf(remoteAchievement),
            reminders = listOf(remoteReminder),
        )
        settings.lastRemoteUpdatedAt = baseline
        settings.lastLocalDatabaseChangeAt = instant(35)
        database.snapshot = localDatabase
        remoteDataSource.remote = remoteSnapshot(
            updatedAt = instant(40),
            databaseUpdatedAt = instant(40),
            settingsUpdatedAt = baseline,
            database = remoteDatabase,
        )

        // when
        createManager(backgroundScope).syncNow()

        // then
        val mergedDatabase = remoteDataSource.writtenSnapshot?.database
        assertEquals(listOf(remoteExercise, duplicateExercise, localExercise), mergedDatabase?.exercises)
        assertEquals(listOf(remoteAchievement), mergedDatabase?.achievements)
        assertEquals(listOf(remoteReminder), mergedDatabase?.reminders)
        assertEquals(mergedDatabase, database.replacedSnapshot)
        assertEquals(timeUtils.now(), remoteDataSource.writtenSnapshot?.databaseUpdatedAt)
        assertEquals(timeUtils.now(), remoteDataSource.writtenSnapshot?.updatedAt)
    }

    @Test
    fun `when local settings are newer than remote settings then local settings are uploaded`() = runTest {
        // given
        val baseline = instant(20)
        settings.lastRemoteUpdatedAt = baseline
        settings.lastLocalSettingsChangeAt = instant(45)
        settings.blinkBreakCount = 95
        remoteDataSource.remote = remoteSnapshot(
            updatedAt = instant(40),
            databaseUpdatedAt = baseline,
            settingsUpdatedAt = instant(40),
            settings = settingsSnapshot(blinkBreakCount = 70),
        )

        // when
        createManager(backgroundScope).syncNow()

        // then
        assertEquals(settingsSnapshot(blinkBreakCount = 95), remoteDataSource.writtenSnapshot?.settings)
        assertEquals(instant(45), remoteDataSource.writtenSnapshot?.settingsUpdatedAt)
        assertNull(database.replacedSnapshot)
    }

    @Test
    fun `when remote settings are newer than local settings then remote settings are applied`() = runTest {
        // given
        val baseline = instant(20)
        settings.lastRemoteUpdatedAt = baseline
        settings.lastLocalSettingsChangeAt = instant(35)
        settings.blinkBreakCount = 95
        remoteDataSource.remote = remoteSnapshot(
            updatedAt = instant(40),
            databaseUpdatedAt = baseline,
            settingsUpdatedAt = instant(40),
            settings = settingsSnapshot(blinkBreakCount = 70),
        )

        // when
        createManager(backgroundScope).syncNow()

        // then
        assertEquals(70, settings.blinkBreakCount)
        assertNull(remoteDataSource.writtenSnapshot)
    }

    @Test
    fun `when local settings and remote database changed then composite snapshot is uploaded and remote database is applied`() =
        runTest {
            // given
            val baseline = instant(20)
            val remoteDatabase = databaseSnapshot(exercises = listOf(exercise(40)))
            settings.lastRemoteUpdatedAt = baseline
            settings.lastLocalSettingsChangeAt = instant(35)
            settings.blinkBreakCount = 95
            remoteDataSource.remote = remoteSnapshot(
                updatedAt = instant(40),
                databaseUpdatedAt = instant(40),
                settingsUpdatedAt = baseline,
                database = remoteDatabase,
                settings = settingsSnapshot(blinkBreakCount = 70),
            )

            // when
            createManager(backgroundScope).syncNow()

            // then
            assertEquals(remoteDatabase, remoteDataSource.writtenSnapshot?.database)
            assertEquals(settingsSnapshot(blinkBreakCount = 95), remoteDataSource.writtenSnapshot?.settings)
            assertEquals(remoteDatabase, database.replacedSnapshot)
            assertEquals(95, settings.blinkBreakCount)
        }

    @Test
    fun `when neither side changed since baseline then only sync timestamp is updated`() = runTest {
        // given
        val baseline = instant(20)
        settings.lastRemoteUpdatedAt = baseline
        val localDatabase = databaseSnapshot(exercises = listOf(exercise(10)))
        val localSettings = settingsSnapshot(blinkBreakCount = 90)
        database.snapshot = localDatabase
        settings.blinkBreakCount = 90
        remoteDataSource.remote = remoteSnapshot(
            updatedAt = baseline,
            databaseUpdatedAt = baseline,
            settingsUpdatedAt = baseline,
            database = localDatabase,
            settings = localSettings,
        )

        // when
        createManager(backgroundScope).syncNow()

        // then
        assertNull(remoteDataSource.writtenSnapshot)
        assertNull(database.replacedSnapshot)
        assertEquals(timeUtils.now(), settings.lastSyncedAt)
        assertEquals(baseline, settings.lastRemoteUpdatedAt)
    }

    private fun createManager(scope: CoroutineScope): BlinklySyncManagerImpl =
        BlinklySyncManagerImpl(
            authService = authService,
            database = database,
            settings = settings,
            remoteDataSource = remoteDataSource,
            timeUtils = timeUtils,
            scope = scope,
            rescheduleReminders = { rescheduleCount += 1 },
        )

    private fun databaseSnapshot(
        exercises: List<Exercise> = emptyList(),
        achievements: List<Achievement> = emptyList(),
        reminders: List<Reminder> = emptyList(),
        reminderSchedules: List<ReminderSchedule> = reminders
            .distinctBy(Reminder::scheduleId)
            .map { reminder ->
                ReminderSchedule(
                    id = reminder.scheduleId,
                    reminderType = reminder.type,
                    configuration = ReminderScheduleConfiguration.Daily(reminder.date.time),
                )
            },
    ): BlinklyDatabaseSnapshot =
        BlinklyDatabaseSnapshot(
            exercises = exercises,
            achievements = achievements,
            reminderSchedules = reminderSchedules,
            reminders = reminders,
        )

    private fun remoteSnapshot(
        updatedAt: Instant,
        database: BlinklyDatabaseSnapshot = databaseSnapshot(),
        settings: BlinklySettingsSnapshot = settingsSnapshot(),
        databaseUpdatedAt: Instant = updatedAt,
        settingsUpdatedAt: Instant = updatedAt,
    ): RemoteBlinklySnapshot =
        RemoteBlinklySnapshot(
            updatedAt = updatedAt,
            lastSyncedAt = null,
            settings = settings,
            database = database,
            databaseUpdatedAt = databaseUpdatedAt,
            settingsUpdatedAt = settingsUpdatedAt,
        )

    private fun settingsSnapshot(blinkBreakCount: Int = 60): BlinklySettingsSnapshot =
        BlinklySettingsSnapshot(
            blinkBreakCount = blinkBreakCount,
            nearFarFocusCount = 10,
            nearFarFocusDuration = 5f,
            diagonalGazesCount = 5,
            diagonalGazesDuration = 3f,
            figureEightCount = 10,
            clockRollsEachSide = 5,
            palmingDuration = 120,
            themeState = ThemeState.SYSTEM,
            lightThemeWorkoutIndex = 0,
            darkThemeWorkoutIndex = 0,
            lastTreeProgressCheckDate = null,
            displayedHighlights = emptyList(),
            currentHighlightDate = null,
            onboardingDisplayed = false,
        )

    private fun exercise(
        minute: Int,
        block: ExerciseBlock = ExerciseBlock.A,
    ): Exercise =
        Exercise(
            block = block,
            type = ExerciseType.BLINK_BREAK,
            completedAt = instant(minute),
        )

    private fun achievement(
        type: AchievementType,
        unlockedAt: Instant?,
    ): Achievement =
        Achievement(
            type = type,
            level = AchievementLevel.BEGINNER,
            unlockedAt = unlockedAt,
        )

    private fun reminder(uuid: String, minute: Int): Reminder =
        Reminder(
            uuid = uuid,
            scheduleId = "schedule",
            date = LocalDateTime(year = 2026, month = 1, day = 1, hour = 9, minute = minute),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.DAILY,
        )

    private fun instant(minute: Int): Instant =
        Instant.fromEpochMilliseconds(minute * 60_000L)

    private class FakeBlinklyAuthService(user: BlinklyUser?) : BlinklyAuthService {
        private val userFlow: MutableStateFlow<BlinklyUser?> = MutableStateFlow(user)

        override val currentUser: Flow<BlinklyUser?> = userFlow

        override suspend fun completeGoogleSignIn(user: BlinklyUser): Result<BlinklyUser> {
            userFlow.value = user
            return Result.success(user)
        }

        override suspend fun signOut(): Result<Unit> {
            userFlow.value = null
            return Result.success(Unit)
        }
    }

    private class FakeRemoteSyncDataSource : BlinklyRemoteSyncDataSource {
        var remote: RemoteBlinklySnapshot? = null
        var writtenSnapshot: RemoteBlinklySnapshot? = null

        override suspend fun readSnapshot(userId: String): Result<RemoteBlinklySnapshot?> =
            Result.success(remote)

        override suspend fun writeSnapshot(userId: String, snapshot: RemoteBlinklySnapshot): Result<Unit> {
            writtenSnapshot = snapshot
            remote = snapshot
            return Result.success(Unit)
        }
    }

    private class FakeBlinklyDatabase : BlinklyDatabase {
        var snapshot: BlinklyDatabaseSnapshot = BlinklyDatabaseSnapshot(emptyList(), emptyList(), emptyList(), emptyList())
        var replacedSnapshot: BlinklyDatabaseSnapshot? = null

        override fun currentCalendar(): Flow<List<Workout>> = flowOf(emptyList())
        override fun currentAchievements(): Flow<List<Achievement>> = flowOf(snapshot.achievements)
        override fun currentReminderSchedules(): Flow<List<ReminderSchedule>> = flowOf(snapshot.reminderSchedules)
        override fun currentReminders(): Flow<List<Reminder>> = flowOf(snapshot.reminders)

        override suspend fun currentSnapshot(): BlinklyDatabaseSnapshot = snapshot

        override suspend fun replaceSnapshot(snapshot: BlinklyDatabaseSnapshot) {
            this.snapshot = snapshot
            replacedSnapshot = snapshot
        }

        override suspend fun saveExercise(exercise: Exercise) {
            snapshot = snapshot.copy(exercises = snapshot.exercises + exercise)
        }

        override suspend fun saveExercises(exercises: List<Exercise>) {
            snapshot = snapshot.copy(exercises = snapshot.exercises + exercises)
        }

        override suspend fun unlockAchievement(achievement: Achievement): Boolean {
            snapshot = snapshot.copy(achievements = snapshot.achievements + achievement)
            return true
        }

        override suspend fun saveAchievements(achievements: List<Achievement>) {
            snapshot = snapshot.copy(achievements = snapshot.achievements + achievements)
        }

        override suspend fun deleteAchievements() {
            snapshot = snapshot.copy(achievements = emptyList())
        }

        override suspend fun deleteExercises() {
            snapshot = snapshot.copy(exercises = emptyList())
        }

        override suspend fun remindersBySchedule(scheduleId: String): List<Reminder> =
            snapshot.reminders.filter { it.scheduleId == scheduleId }

        override suspend fun saveReminderSchedule(schedule: ReminderSchedule, reminders: List<Reminder>) {
            snapshot = snapshot.copy(
                reminderSchedules = snapshot.reminderSchedules.filterNot { it.id == schedule.id } + schedule,
                reminders = snapshot.reminders.filterNot { it.scheduleId == schedule.id } + reminders,
            )
        }

        override suspend fun deleteReminderSchedule(scheduleId: String) {
            snapshot = snapshot.copy(
                reminderSchedules = snapshot.reminderSchedules.filterNot { it.id == scheduleId },
                reminders = snapshot.reminders.filterNot { it.scheduleId == scheduleId },
            )
        }

        override suspend fun deleteReminders() {
            snapshot = snapshot.copy(reminderSchedules = emptyList(), reminders = emptyList())
        }
    }

    private class FakeBlinklySettings : BlinklySettings {
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
        override var lastLocalDatabaseChangeAt: Instant? = null
        override var lastLocalSettingsChangeAt: Instant? = null
        override var lastSyncedAt: Instant? = null
        override var lastRemoteUpdatedAt: Instant? = null
    }

    private class FakeTimeUtils(private val now: Instant) : BlinklyTimeUtils {
        override fun now(): Instant = now
        override fun timeZone(): TimeZone = TimeZone.UTC
    }
}
