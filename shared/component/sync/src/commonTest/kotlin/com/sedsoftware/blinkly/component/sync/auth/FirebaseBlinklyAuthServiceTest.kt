package com.sedsoftware.blinkly.component.sync.auth

import com.sedsoftware.blinkly.domain.model.BlinklyUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FirebaseBlinklyAuthServiceTest {

    private val callbackUser = BlinklyUser(
        id = "callback-user",
        displayName = "Callback",
        email = "callback@example.com",
    )

    @Test
    fun `Google sign in is rejected when native Firebase user is absent`() {
        val result = validateGoogleSignInUser(callbackUser = callbackUser, nativeUser = null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `Google sign in is rejected when native Firebase uid differs`() {
        val nativeUser = callbackUser.copy(id = "native-user")

        val result = validateGoogleSignInUser(callbackUser = callbackUser, nativeUser = nativeUser)

        assertTrue(result.isFailure)
    }

    @Test
    fun `Google sign in returns native Firebase profile when uid matches`() {
        val nativeUser = callbackUser.copy(displayName = "Native", email = "native@example.com")

        val result = validateGoogleSignInUser(callbackUser = callbackUser, nativeUser = nativeUser)

        assertEquals(nativeUser, result.getOrThrow())
    }
}
