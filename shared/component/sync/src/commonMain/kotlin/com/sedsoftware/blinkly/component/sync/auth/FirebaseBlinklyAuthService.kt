package com.sedsoftware.blinkly.component.sync.auth

import com.sedsoftware.blinkly.domain.external.BlinklyAuthService
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FirebaseBlinklyAuthService(
    scope: CoroutineScope,
    private val auth: FirebaseAuth = Firebase.auth,
) : BlinklyAuthService {

    override val currentUser: StateFlow<BlinklyUser?> =
        auth.authStateChanged
            .map { user -> user?.toBlinklyUser() }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = auth.currentUser?.toBlinklyUser(),
            )

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
