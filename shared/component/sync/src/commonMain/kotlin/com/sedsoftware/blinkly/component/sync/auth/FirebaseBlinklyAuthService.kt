package com.sedsoftware.blinkly.component.sync.auth

import com.sedsoftware.blinkly.domain.external.BlinklyAuthService
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseBlinklyAuthService(
    private val auth: FirebaseAuth = Firebase.auth,
) : BlinklyAuthService {

    override val currentUser: Flow<BlinklyUser?> =
        auth.authStateChanged.map { user -> user?.toBlinklyUser() }

    override suspend fun signInWithGoogle(): Result<BlinklyUser> =
        Result.failure(UnsupportedOperationException("Google Sign-In is initiated from Compose through KMPAuth"))

    override suspend fun completeGoogleSignIn(user: BlinklyUser): Result<BlinklyUser> =
        Result.success(user)

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            auth.signOut()
        }
}

fun FirebaseUser.toBlinklyUser(): BlinklyUser =
    BlinklyUser(
        id = uid,
        displayName = displayName,
        email = email,
    )
