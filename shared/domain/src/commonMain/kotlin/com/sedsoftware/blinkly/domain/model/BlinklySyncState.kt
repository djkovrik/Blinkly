package com.sedsoftware.blinkly.domain.model

import kotlin.time.Instant

data class BlinklySyncState(
    val isAuthorized: Boolean,
    val isSyncing: Boolean,
    val lastSyncedAt: Instant?,
    val error: BlinklyError?,
)
