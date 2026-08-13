package com.sedsoftware.blinkly.domain.model

import kotlin.time.Instant

data class BlinklySyncState(
    val authSession: BlinklyAuthSession,
    val isSyncing: Boolean,
    val lastSyncedAt: Instant?,
    val error: BlinklyError?,
) {
    val isAuthorized: Boolean
        get() = authSession is BlinklyAuthSession.SignedIn
}
