package com.sedsoftware.blinkly.compose.auth

import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider

fun initializeBlinklyGoogleAuth(serverId: String) {
    if (serverId.isNotBlank()) {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(serverId = serverId)
        )
    }
}
