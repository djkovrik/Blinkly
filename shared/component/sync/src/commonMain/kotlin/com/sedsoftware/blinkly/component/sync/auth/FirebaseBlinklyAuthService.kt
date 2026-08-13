package com.sedsoftware.blinkly.component.sync.auth

import com.sedsoftware.blinkly.domain.external.BlinklyAuthService
import com.sedsoftware.blinkly.domain.model.BlinklyAuthSession
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

    override val session: StateFlow<BlinklyAuthSession> =
        auth.authStateChanged
            .map { user ->
                user
                    ?.toBlinklyUser()
                    ?.let(BlinklyAuthSession::SignedIn)
                    ?: BlinklyAuthSession.SignedOut
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = auth.currentUser
                    ?.toBlinklyUser()
                    ?.let(BlinklyAuthSession::SignedIn)
                    ?: BlinklyAuthSession.Restoring,
            )

    override suspend fun completeGoogleSignIn(user: BlinklyUser): Result<BlinklyUser> =
        validateGoogleSignInUser(
            callbackUser = user,
            nativeUser = auth.currentUser?.toBlinklyUser(),
        )

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            auth.signOut()
        }
}

internal fun validateGoogleSignInUser(
    callbackUser: BlinklyUser,
    nativeUser: BlinklyUser?,
): Result<BlinklyUser> =
    runCatching {
        checkNotNull(nativeUser) { "Google Sign-In completed without a native Firebase session" }
        check(nativeUser.id == callbackUser.id) { "Google Sign-In user does not match the native Firebase session" }
        nativeUser
    }

fun FirebaseUser.toBlinklyUser(): BlinklyUser =
    BlinklyUser(
        id = uid,
        displayName = displayName,
        email = email,
    )
