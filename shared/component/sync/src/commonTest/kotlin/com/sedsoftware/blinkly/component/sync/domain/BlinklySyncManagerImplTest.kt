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
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
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
    private val timeUtils: FakeTimeUtils = FakeTimeUtils(now = instant(20))

    @Test
    fun `when remote snapshot is missing then local snapshot is uploaded`() = runTest {
        // given
        val localChangedAt = instant(10)
        val localSnapshot = databaseSnapshot(exercises = listOf(exercise(10)))
        settings.lastLocalChangeAt = localChangedAt
        database.snapshot = localSnapshot
        remoteDataSource.remote = null

        // when
        createManager(backgroundScope).syncNow()

        // then
        assertEquals(localSnapshot, remoteDataSource.writtenSnapshot?.database)
        assertEquals(localChangedAt, remoteDataSource.writtenSnapshot?.updatedAt)
        assertEquals(timeUtils.now(), settings.lastSyncedAt)
        assertEquals(localChangedAt, settings.lastRemoteUpdatedAt)
        assertNull(database.replacedSnapshot)
    }

    @Test
    fun `when local snapshot is newer than remote then local snapshot is uploaded`() = runTest {
        // given
        val localChangedAt = instant(30)
        val remoteUpdatedAt = instant(10)
        val localSnapshot = databaseSnapshot(exercises = listOf(exercise(30)))
        settings.lastLocalChangeAt = localChangedAt
        database.snapshot = localSnapshot
        remoteDataSource.remote = remoteSnapshot(remoteUpdatedAt, databaseSnapshot(exercises = listOf(exercise(10))))

        // when
        createManager(backgroundScope).syncNow()

        // then
        assertEquals(localSnapshot, remoteDataSource.writtenSnapshot?.database)
        assertEquals(localChangedAt, remoteDataSource.writtenSnapshot?.updatedAt)
        assertNull(database.replacedSnapshot)
    }

    @Test
    fun `when remote snapshot is newer than local then remote snapshot is applied`() = runTest {
        // given
        val remoteUpdatedAt = instant(30)
        val remoteDatabase = databaseSnapshot(exercises = listOf(exercise(30)))
        val remoteSettings = settingsSnapshot(blinkBreakCount = 90)
        settings.lastLocalChangeAt = instant(10)
        remoteDataSource.remote = remoteSnapshot(
            updatedAt = remoteUpdatedAt,
            database = remoteDatabase,
            settings = remoteSettings,
        )

        // when
        createManager(backgroundScope).syncNow()

        // then
        assertEquals(remoteDatabase, database.replacedSnapshot)
        assertEquals(90, settings.blinkBreakCount)
        assertEquals(timeUtils.now(), settings.lastSyncedAt)
        assertEquals(remoteUpdatedAt, settings.lastRemoteUpdatedAt)
        assertNull(remoteDataSource.writtenSnapshot)
    }

    @Test
    fun `when timestamps are missing and both sides have data then snapshots are merged and uploaded`() = runTest {
        // given
        val localExercise = exercise(10, ExerciseBlock.A)
        val remoteExercise = exercise(11, ExerciseBlock.B)
        val duplicateExercise = exercise(12, ExerciseBlock.C)
        val localAchievement = achievement(AchievementType.FIRST_SPARK, unlockedAt = instant(12))
        val remoteAchievement = achievement(AchievementType.FIRST_SPARK, unlockedAt = instant(10))
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
        settings.lastLocalChangeAt = null
        database.snapshot = localDatabase
        remoteDataSource.remote = remoteSnapshot(instant(9), remoteDatabase)

        // when
        createManager(backgroundScope).syncNow()

        // then
        val mergedDatabase = remoteDataSource.writtenSnapshot?.database
        assertEquals(listOf(remoteExercise, duplicateExercise, localExercise), mergedDatabase?.exercises)
        assertEquals(listOf(remoteAchievement), mergedDatabase?.achievements)
        assertEquals(listOf(remoteReminder), mergedDatabase?.reminders)
        assertEquals(mergedDatabase, database.replacedSnapshot)
        assertEquals(timeUtils.now(), remoteDataSource.writtenSnapshot?.updatedAt)
    }

    private fun createManager(scope: CoroutineScope): BlinklySyncManagerImpl =
        BlinklySyncManagerImpl(
            authService = authService,
            database = database,
            settings = settings,
            remoteDataSource = remoteDataSource,
            timeUtils = timeUtils,
            scope = scope,
        )

    private fun databaseSnapshot(
        exercises: List<Exercise> = emptyList(),
        achievements: List<Achievement> = emptyList(),
        reminders: List<Reminder> = emptyList(),
    ): BlinklyDatabaseSnapshot =
        BlinklyDatabaseSnapshot(
            exercises = exercises,
            achievements = achievements,
            reminders = reminders,
        )

    private fun remoteSnapshot(
        updatedAt: Instant,
        database: BlinklyDatabaseSnapshot,
        settings: BlinklySettingsSnapshot = settingsSnapshot(),
    ): RemoteBlinklySnapshot =
        RemoteBlinklySnapshot(
            updatedAt = updatedAt,
            lastSyncedAt = null,
            settings = settings,
            database = database,
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
            date = LocalDateTime(year = 2026, month = 1, day = 1, hour = 9, minute = minute),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.DAILY,
            weekDays = listOf(DayOfWeek.MONDAY),
        )

    private fun instant(minute: Int): Instant =
        Instant.fromEpochMilliseconds(minute * 60_000L)

    private class FakeBlinklyAuthService(user: BlinklyUser?) : BlinklyAuthService {
        private val userFlow: MutableStateFlow<BlinklyUser?> = MutableStateFlow(user)

        override val currentUser: Flow<BlinklyUser?> = userFlow

        override suspend fun signInWithGoogle(): Result<BlinklyUser> =
            Result.success(requireNotNull(userFlow.value))

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
        var snapshot: BlinklyDatabaseSnapshot = BlinklyDatabaseSnapshot(emptyList(), emptyList(), emptyList())
        var replacedSnapshot: BlinklyDatabaseSnapshot? = null

        override fun currentCalendar(): Flow<List<Workout>> = flowOf(emptyList())
        override fun currentAchievements(): Flow<List<Achievement>> = flowOf(snapshot.achievements)
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

        override suspend fun unlockAchievement(achievement: Achievement) {
            snapshot = snapshot.copy(achievements = snapshot.achievements + achievement)
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

        override suspend fun saveReminder(reminder: Reminder) {
            snapshot = snapshot.copy(reminders = snapshot.reminders + reminder)
        }

        override suspend fun saveReminders(reminders: List<Reminder>) {
            snapshot = snapshot.copy(reminders = snapshot.reminders + reminders)
        }

        override suspend fun deleteReminder(uuid: String) {
            snapshot = snapshot.copy(reminders = snapshot.reminders.filterNot { it.uuid == uuid })
        }

        override suspend fun deleteReminders() {
            snapshot = snapshot.copy(reminders = emptyList())
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
        override var lastLocalChangeAt: Instant? = null
        override var lastSyncedAt: Instant? = null
        override var lastRemoteUpdatedAt: Instant? = null
    }

    private class FakeTimeUtils(private val now: Instant) : BlinklyTimeUtils {
        override fun now(): Instant = now
        override fun timeZone(): TimeZone = TimeZone.UTC
    }
}
