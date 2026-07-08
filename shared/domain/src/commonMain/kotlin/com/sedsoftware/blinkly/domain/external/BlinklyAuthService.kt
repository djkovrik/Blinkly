package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.BlinklyUser
import kotlinx.coroutines.flow.Flow

interface BlinklyAuthService {
    val currentUser: Flow<BlinklyUser?>
    suspend fun signInWithGoogle(): Result<BlinklyUser>
    suspend fun completeGoogleSignIn(user: BlinklyUser): Result<BlinklyUser>
    suspend fun signOut(): Result<Unit>
}
