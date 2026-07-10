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
            val localDatabaseChangedAt = settings.lastLocalDatabaseChangeAt
            val localSettingsChangedAt = settings.lastLocalSettingsChangeAt
            val lastRemoteUpdatedAt = settings.lastRemoteUpdatedAt
            val remote = remoteDataSource
                .readSnapshot(user.id)
                .getOrElse { throwable -> throw throwable.asBlinklyError(BlinklyError::SyncReadFailed) }

            val now = timeUtils.now()
            val syncedRemoteUpdatedAt = if (remote == null) {
                uploadLocal(
                    userId = user.id,
                    databaseSnapshot = localDatabase,
                    settingsSnapshot = localSettings,
                    databaseUpdatedAt = localDatabaseChangedAt ?: now,
                    settingsUpdatedAt = localSettingsChangedAt ?: now,
                    syncedAt = now,
                ).updatedAt
            } else {
                syncExistingRemote(
                    userId = user.id,
                    localDatabase = localDatabase,
                    localSettings = localSettings,
                    localDatabaseChangedAt = localDatabaseChangedAt,
                    localSettingsChangedAt = localSettingsChangedAt,
                    lastRemoteUpdatedAt = lastRemoteUpdatedAt,
                    remote = remote,
                    now = now,
                )
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
        databaseUpdatedAt: Instant,
        settingsUpdatedAt: Instant,
        syncedAt: Instant,
    ): RemoteBlinklySnapshot {
        val snapshot = RemoteBlinklySnapshot(
            updatedAt = syncedAt,
            lastSyncedAt = syncedAt,
            settings = settingsSnapshot,
            database = databaseSnapshot,
            databaseUpdatedAt = databaseUpdatedAt,
            settingsUpdatedAt = settingsUpdatedAt,
        )

        remoteDataSource
            .writeSnapshot(userId, snapshot)
            .getOrElse { throwable -> throw throwable.asBlinklyError(BlinklyError::SyncWriteFailed) }

        return snapshot
    }

    private suspend fun syncExistingRemote(
        userId: String,
        localDatabase: BlinklyDatabaseSnapshot,
        localSettings: BlinklySettingsSnapshot,
        localDatabaseChangedAt: Instant?,
        localSettingsChangedAt: Instant?,
        lastRemoteUpdatedAt: Instant?,
        remote: RemoteBlinklySnapshot,
        now: Instant,
    ): Instant {
        val localDatabaseChanged = hasLocalDatabaseChanges(localDatabase, localDatabaseChangedAt, lastRemoteUpdatedAt)
        val localSettingsChanged = hasLocalSettingsChanges(localSettingsChangedAt, lastRemoteUpdatedAt)
        val remoteDatabaseChanged = hasRemoteChanges(remote.databaseUpdatedAt, lastRemoteUpdatedAt)
        val remoteSettingsChanged = hasRemoteChanges(remote.settingsUpdatedAt, lastRemoteUpdatedAt)

        val resolvedDatabase = resolveDatabase(
            local = localDatabase,
            localChanged = localDatabaseChanged,
            localChangedAt = localDatabaseChangedAt,
            remote = remote,
            remoteChanged = remoteDatabaseChanged,
            now = now,
        )
        val resolvedSettings = resolveSettings(
            local = localSettings,
            localChanged = localSettingsChanged,
            localChangedAt = localSettingsChangedAt,
            remote = remote,
            remoteChanged = remoteSettingsChanged,
            now = now,
        )

        val remoteNeedsWrite = resolvedDatabase.snapshot != remote.database ||
            resolvedSettings.snapshot != remote.settings ||
            resolvedDatabase.updatedAt != remote.databaseUpdatedAt ||
            resolvedSettings.updatedAt != remote.settingsUpdatedAt

        val syncedRemote = if (remoteNeedsWrite) {
            remote.copy(
                updatedAt = now,
                lastSyncedAt = now,
                settings = resolvedSettings.snapshot,
                database = resolvedDatabase.snapshot,
                databaseUpdatedAt = resolvedDatabase.updatedAt,
                settingsUpdatedAt = resolvedSettings.updatedAt,
            )
                .also { snapshot ->
                    remoteDataSource
                        .writeSnapshot(userId, snapshot)
                        .getOrElse { throwable -> throw throwable.asBlinklyError(BlinklyError::SyncWriteFailed) }
                }
        } else {
            remote
        }

        if (resolvedDatabase.snapshot != localDatabase) {
            database.replaceSnapshot(resolvedDatabase.snapshot)
        }

        if (resolvedSettings.snapshot != localSettings) {
            settings.applySnapshot(resolvedSettings.snapshot)
        }

        return syncedRemote.updatedAt
    }

    private fun hasLocalDatabaseChanges(
        localDatabase: BlinklyDatabaseSnapshot,
        localChangedAt: Instant?,
        lastRemoteUpdatedAt: Instant?,
    ): Boolean =
        isAfterBaseline(localChangedAt, lastRemoteUpdatedAt) ||
            lastRemoteUpdatedAt == null && localChangedAt == null && localDatabase.isNotEmpty()

    private fun hasLocalSettingsChanges(localChangedAt: Instant?, lastRemoteUpdatedAt: Instant?): Boolean =
        isAfterBaseline(localChangedAt, lastRemoteUpdatedAt)

    private fun hasRemoteChanges(remoteChangedAt: Instant, lastRemoteUpdatedAt: Instant?): Boolean =
        lastRemoteUpdatedAt == null || remoteChangedAt > lastRemoteUpdatedAt

    private fun isAfterBaseline(changedAt: Instant?, lastRemoteUpdatedAt: Instant?): Boolean =
        changedAt != null && (lastRemoteUpdatedAt == null || changedAt > lastRemoteUpdatedAt)

    private fun resolveDatabase(
        local: BlinklyDatabaseSnapshot,
        localChanged: Boolean,
        localChangedAt: Instant?,
        remote: RemoteBlinklySnapshot,
        remoteChanged: Boolean,
        now: Instant,
    ): ResolvedSnapshot<BlinklyDatabaseSnapshot> =
        when {
            localChanged && remoteChanged ->
                ResolvedSnapshot(
                    snapshot = mergeDatabaseSnapshots(local, remote.database),
                    updatedAt = now,
                )

            localChanged ->
                ResolvedSnapshot(
                    snapshot = local,
                    updatedAt = localChangedAt ?: now,
                )

            else ->
                ResolvedSnapshot(
                    snapshot = remote.database,
                    updatedAt = remote.databaseUpdatedAt,
                )
        }

    private fun resolveSettings(
        local: BlinklySettingsSnapshot,
        localChanged: Boolean,
        localChangedAt: Instant?,
        remote: RemoteBlinklySnapshot,
        remoteChanged: Boolean,
        now: Instant,
    ): ResolvedSnapshot<BlinklySettingsSnapshot> =
        when {
            localChanged && remoteChanged && localChangedAt != null && localChangedAt > remote.settingsUpdatedAt ->
                ResolvedSnapshot(
                    snapshot = local,
                    updatedAt = localChangedAt,
                )

            localChanged && remoteChanged ->
                ResolvedSnapshot(
                    snapshot = remote.settings,
                    updatedAt = remote.settingsUpdatedAt,
                )

            localChanged ->
                ResolvedSnapshot(
                    snapshot = local,
                    updatedAt = localChangedAt ?: now,
                )

            else ->
                ResolvedSnapshot(
                    snapshot = remote.settings,
                    updatedAt = remote.settingsUpdatedAt,
                )
        }

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

    private data class ResolvedSnapshot<T>(
        val snapshot: T,
        val updatedAt: Instant,
    )
}

private fun Exercise.syncKey(): String =
    "${type.name}:${block.name}:${completedAt.toEpochMilliseconds()}"
