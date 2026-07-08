package com.sedsoftware.blinkly.component.sync.remote

import com.sedsoftware.blinkly.component.sync.dto.BlinklyRemoteSnapshotDto
import com.sedsoftware.blinkly.component.sync.mapper.toDomain
import com.sedsoftware.blinkly.component.sync.mapper.toDto
import com.sedsoftware.blinkly.domain.external.BlinklyRemoteSyncDataSource
import com.sedsoftware.blinkly.domain.model.RemoteBlinklySnapshot
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

class FirestoreSyncDataSource(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : BlinklyRemoteSyncDataSource {

    override suspend fun readSnapshot(userId: String): Result<RemoteBlinklySnapshot?> =
        runCatching {
            val snapshot = document(userId).get()
            if (snapshot.exists) {
                snapshot.data(BlinklyRemoteSnapshotDto.serializer()).toDomain()
            } else {
                null
            }
        }

    override suspend fun writeSnapshot(userId: String, snapshot: RemoteBlinklySnapshot): Result<Unit> =
        runCatching {
            document(userId).set(BlinklyRemoteSnapshotDto.serializer(), snapshot.toDto()) {
                encodeDefaults = true
            }
        }

    private fun document(userId: String) =
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(SYNC_COLLECTION)
            .document(MAIN_DOCUMENT)

    private companion object {
        const val USERS_COLLECTION = "users"
        const val SYNC_COLLECTION = "sync"
        const val MAIN_DOCUMENT = "main"
    }
}
