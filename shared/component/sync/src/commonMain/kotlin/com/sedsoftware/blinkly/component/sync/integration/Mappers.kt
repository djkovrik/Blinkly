package com.sedsoftware.blinkly.component.sync.integration

import com.sedsoftware.blinkly.component.sync.BlinklySyncComponent
import com.sedsoftware.blinkly.component.sync.store.BlinklySyncStore

internal val stateToModel: (BlinklySyncStore.State) -> BlinklySyncComponent.Model = { state ->
    BlinklySyncComponent.Model(
        isAuthorized = state.isAuthorized,
        isSyncing = state.isSyncing,
        status = when {
            state.isSyncing -> BlinklySyncComponent.Status.Syncing
            state.error != null -> BlinklySyncComponent.Status.Failed(state.error.message)
            state.lastSyncedAt != null -> BlinklySyncComponent.Status.Synced(state.lastSyncedAt)
            else -> BlinklySyncComponent.Status.NotSynced
        },
        lastSyncedAt = state.lastSyncedAt,
        buttonMode = if (state.isAuthorized) {
            BlinklySyncComponent.ButtonMode.Sync
        } else {
            BlinklySyncComponent.ButtonMode.SignIn
        },
    )
}
