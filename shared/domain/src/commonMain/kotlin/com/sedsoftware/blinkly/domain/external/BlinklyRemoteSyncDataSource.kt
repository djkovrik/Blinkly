package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.RemoteBlinklySnapshot

interface BlinklyRemoteSyncDataSource {
    suspend fun readSnapshot(userId: String): Result<RemoteBlinklySnapshot?>
    suspend fun writeSnapshot(userId: String, snapshot: RemoteBlinklySnapshot): Result<Unit>
}
