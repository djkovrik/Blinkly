package com.sedsoftware.blinkly.domain.model

sealed interface BlinklyAuthSession {
    data object Restoring : BlinklyAuthSession
    data class SignedIn(val user: BlinklyUser) : BlinklyAuthSession
    data object SignedOut : BlinklyAuthSession
}
