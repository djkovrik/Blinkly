package com.sedsoftware.blinkly.component.sync

import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import kotlin.time.Instant

interface BlinklySyncComponent {

    val model: Value<Model>

    fun onPrimaryButtonClick()
    fun onGoogleSignInCompleted(user: BlinklyUser)
    fun onGoogleSignInFailed(throwable: Throwable)

    data class Model(
        val isAuthorized: Boolean,
        val isSyncing: Boolean,
        val status: Status,
        val lastSyncedAt: Instant?,
        val buttonMode: ButtonMode,
    )

    enum class ButtonMode {
        SignIn,
        Sync,
    }

    sealed interface Status {
        data object NotSynced : Status
        data object Syncing : Status
        data class Synced(val at: Instant) : Status
        data class Failed(val message: String?) : Status
    }
}
