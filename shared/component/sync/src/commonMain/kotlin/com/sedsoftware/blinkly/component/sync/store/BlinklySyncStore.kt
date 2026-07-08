package com.sedsoftware.blinkly.component.sync.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import kotlin.time.Instant

internal interface BlinklySyncStore : Store<BlinklySyncStore.Intent, BlinklySyncStore.State, BlinklySyncStore.Label> {

    sealed interface Intent {
        data object PrimaryButtonClicked : Intent
        data class GoogleSignInCompleted(val user: BlinklyUser) : Intent
        data class GoogleSignInFailed(val throwable: Throwable) : Intent
    }

    data class State(
        val isAuthorized: Boolean = false,
        val isSyncing: Boolean = false,
        val lastSyncedAt: Instant? = null,
        val error: BlinklyError? = null,
    )

    sealed interface Label {
        data object RequestGoogleSignIn : Label
        data class ErrorCaught(val exception: BlinklyError) : Label
    }
}
