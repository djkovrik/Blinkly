package com.sedsoftware.blinkly.domain.model

import kotlin.time.Instant

data class RemoteBlinklySnapshot(
    val updatedAt: Instant,
    val lastSyncedAt: Instant?,
    val settings: BlinklySettingsSnapshot,
    val database: BlinklyDatabaseSnapshot,
)
