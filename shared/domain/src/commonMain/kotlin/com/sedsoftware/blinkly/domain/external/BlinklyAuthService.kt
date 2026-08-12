package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.BlinklyUser
import kotlinx.coroutines.flow.StateFlow

interface BlinklyAuthService {
    val currentUser: StateFlow<BlinklyUser?>
    suspend fun completeGoogleSignIn(user: BlinklyUser): Result<BlinklyUser>
    suspend fun signOut(): Result<Unit>
}
