package com.sedsoftware.blinkly.component.sync.integration

import com.sedsoftware.blinkly.component.sync.BlinklySyncComponent
import com.sedsoftware.blinkly.component.sync.store.BlinklySyncStore
import com.sedsoftware.blinkly.domain.model.BlinklyAuthSession

internal val stateToModel: (BlinklySyncStore.State) -> BlinklySyncComponent.Model = { state ->
    BlinklySyncComponent.Model(
        isAuthorized = state.authSession is BlinklyAuthSession.SignedIn,
        isSyncing = state.isSyncing,
        status = when {
            state.authSession is BlinklyAuthSession.Restoring -> BlinklySyncComponent.Status.Restoring
            state.isSyncing -> BlinklySyncComponent.Status.Syncing
            state.error != null -> BlinklySyncComponent.Status.Failed(state.error.message)
            state.lastSyncedAt != null -> BlinklySyncComponent.Status.Synced(state.lastSyncedAt)
            else -> BlinklySyncComponent.Status.NotSynced
        },
        lastSyncedAt = state.lastSyncedAt,
        buttonMode = when (state.authSession) {
            BlinklyAuthSession.Restoring -> BlinklySyncComponent.ButtonMode.Restoring
            is BlinklyAuthSession.SignedIn -> BlinklySyncComponent.ButtonMode.Sync
            BlinklyAuthSession.SignedOut -> BlinklySyncComponent.ButtonMode.SignIn
        },
    )
}
