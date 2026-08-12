package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.BlinklySyncState
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import kotlinx.coroutines.flow.StateFlow

interface BlinklySyncManager {
    val state: StateFlow<BlinklySyncState>
    suspend fun completeGoogleSignIn(user: BlinklyUser)
    suspend fun syncNow()
}
