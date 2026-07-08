package com.sedsoftware.blinkly.component.sync.domain

import com.sedsoftware.blinkly.domain.external.BlinklyAuthService
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyRemoteSyncDataSource
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklySyncManager
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.BlinklySettingsSnapshot
import com.sedsoftware.blinkly.domain.model.BlinklySyncState
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.RemoteBlinklySnapshot
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.applySnapshot
import com.sedsoftware.blinkly.domain.model.asBlinklyError
import com.sedsoftware.blinkly.domain.model.toSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Instant

class BlinklySyncManagerImpl(
    private val authService: BlinklyAuthService,
    private val database: BlinklyDatabase,
    private val settings: BlinklySettings,
    private val remoteDataSource: BlinklyRemoteSyncDataSource,
    private val timeUtils: BlinklyTimeUtils,
    scope: CoroutineScope,
) : BlinklySyncManager {

    private val operationState: MutableStateFlow<OperationState> = MutableStateFlow(OperationState())

    override val state: StateFlow<BlinklySyncState> =
        authService.currentUser
            .combine(operationState) { user, operation ->
                BlinklySyncState(
                    isAuthorized = user != null,
                    isSyncing = operation.isSyncing,
                    lastSyncedAt = settings.lastSyncedAt,
                    error = operation.error,
                )
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = BlinklySyncState(
                    isAuthorized = false,
                    isSyncing = false,
                    lastSyncedAt = settings.lastSyncedAt,
                    error = null,
                ),
            )

    override suspend fun signInOrSync() {
        val user = authService.currentUser.first()
        if (user == null) {
            val authorizedUser = authService
                .signInWithGoogle()
                .getOrElse { throwable -> throw throwable.asBlinklyError(BlinklyError::SyncAuthFailed) }

            syncNow(authorizedUser)
        } else {
            syncNow(user)
        }
    }

    override suspend fun completeGoogleSignIn(user: BlinklyUser) {
        val authorizedUser = authService
            .completeGoogleSignIn(user)
            .getOrElse { throwable -> throw throwable.asBlinklyError(BlinklyError::SyncAuthFailed) }

        syncNow(authorizedUser)
    }

    override suspend fun syncNow() {
        val user = authService
            .currentUser
            .first()
            ?: throw BlinklyError.SyncAuthFailed(IllegalStateException("No authorized user"))

        syncNow(user)
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun syncNow(user: BlinklyUser) {
        operationState.update { it.copy(isSyncing = true, error = null) }

        try {
            val localDatabase = database.currentSnapshot()
            val localSettings = settings.toSnapshot()
            val localChangedAt = settings.lastLocalChangeAt
            val remote = remoteDataSource
                .readSnapshot(user.id)
                .getOrElse { throwable -> throw throwable.asBlinklyError(BlinklyError::SyncReadFailed) }

            val now = timeUtils.now()
            val syncedRemoteUpdatedAt = when {
                remote == null -> uploadLocal(user.id, localDatabase, localSettings, localChangedAt ?: now, now)
                shouldUploadLocal(localChangedAt, remote) -> uploadLocal(user.id, localDatabase, localSettings, localChangedAt ?: now, now)
                shouldMerge(localChangedAt, localDatabase, remote) -> uploadMerged(user.id, localDatabase, localSettings, remote, now)
                shouldApplyRemote(localChangedAt, remote) -> {
                    applyRemote(remote)
                    remote.updatedAt
                }
                else -> remote.updatedAt
            }

            settings.lastSyncedAt = now
            settings.lastRemoteUpdatedAt = syncedRemoteUpdatedAt
            operationState.update { it.copy(isSyncing = false, error = null) }
        } catch (throwable: Throwable) {
            val error = throwable.asBlinklyError(BlinklyError::SyncUnknown)
            operationState.update { it.copy(isSyncing = false, error = error) }
            throw error
        }
    }

    private suspend fun uploadLocal(
        userId: String,
        databaseSnapshot: BlinklyDatabaseSnapshot,
        settingsSnapshot: BlinklySettingsSnapshot,
        updatedAt: Instant,
        syncedAt: Instant,
    ): Instant {
        val snapshot = RemoteBlinklySnapshot(
            updatedAt = updatedAt,
            lastSyncedAt = syncedAt,
            settings = settingsSnapshot,
            database = databaseSnapshot,
        )

        remoteDataSource
            .writeSnapshot(userId, snapshot)
            .getOrElse { throwable -> throw throwable.asBlinklyError(BlinklyError::SyncWriteFailed) }

        return updatedAt
    }

    private suspend fun uploadMerged(
        userId: String,
        localDatabase: BlinklyDatabaseSnapshot,
        localSettings: BlinklySettingsSnapshot,
        remote: RemoteBlinklySnapshot,
        now: Instant,
    ): Instant {
        val merged = remote.copy(
            updatedAt = now,
            lastSyncedAt = now,
            settings = localSettings,
            database = mergeDatabaseSnapshots(localDatabase, remote.database),
        )

        remoteDataSource
            .writeSnapshot(userId, merged)
            .getOrElse { throwable -> throw throwable.asBlinklyError(BlinklyError::SyncWriteFailed) }

        database.replaceSnapshot(merged.database)
        settings.applySnapshot(merged.settings)

        return merged.updatedAt
    }

    private suspend fun applyRemote(remote: RemoteBlinklySnapshot) {
        database.replaceSnapshot(remote.database)
        settings.applySnapshot(remote.settings)
    }

    private fun shouldUploadLocal(localChangedAt: Instant?, remote: RemoteBlinklySnapshot): Boolean =
        localChangedAt != null && localChangedAt > remote.updatedAt

    private fun shouldMerge(
        localChangedAt: Instant?,
        localDatabase: BlinklyDatabaseSnapshot,
        remote: RemoteBlinklySnapshot,
    ): Boolean =
        localChangedAt == null && localDatabase.isNotEmpty() && remote.database.isNotEmpty()

    private fun shouldApplyRemote(localChangedAt: Instant?, remote: RemoteBlinklySnapshot): Boolean =
        localChangedAt == null || remote.updatedAt > localChangedAt

    private fun mergeDatabaseSnapshots(
        local: BlinklyDatabaseSnapshot,
        remote: BlinklyDatabaseSnapshot,
    ): BlinklyDatabaseSnapshot =
        BlinklyDatabaseSnapshot(
            exercises = (remote.exercises + local.exercises).distinctBy(Exercise::syncKey),
            achievements = (remote.achievements + local.achievements)
                .groupBy(Achievement::type)
                .values
                .map(::mergeAchievements),
            reminders = (remote.reminders + local.reminders).distinctBy(Reminder::uuid),
        )

    private fun mergeAchievements(items: List<Achievement>): Achievement {
        val first = items.first()
        val earliestUnlock = items
            .mapNotNull(Achievement::unlockedAt)
            .minOrNull()

        return first.copy(unlockedAt = earliestUnlock ?: first.unlockedAt)
    }

    private fun BlinklyDatabaseSnapshot.isNotEmpty(): Boolean =
        exercises.isNotEmpty() || achievements.isNotEmpty() || reminders.isNotEmpty()

    private data class OperationState(
        val isSyncing: Boolean = false,
        val error: BlinklyError? = null,
    )
}

private fun Exercise.syncKey(): String =
    "${type.name}:${block.name}:${completedAt.toEpochMilliseconds()}"
