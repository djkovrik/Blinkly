package com.sedsoftware.blinkly.component.sync.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.sync.BlinklySyncComponent
import com.sedsoftware.blinkly.component.sync.BlinklySyncComponent.Model
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class BlinklySyncComponentPreview(
    isAuthorized: Boolean = false,
    isSyncing: Boolean = false,
    lastSyncedAt: Instant? = null,
    status: BlinklySyncComponent.Status? = null,
) : BlinklySyncComponent {

    override val model: Value<Model> = MutableValue(
        Model(
            isAuthorized = isAuthorized,
            isSyncing = isSyncing,
            status = status ?: when {
                isSyncing -> BlinklySyncComponent.Status.Syncing
                lastSyncedAt != null -> BlinklySyncComponent.Status.Synced(lastSyncedAt)
                else -> BlinklySyncComponent.Status.NotSynced
            },
            lastSyncedAt = lastSyncedAt,
            buttonMode = if (isAuthorized) {
                BlinklySyncComponent.ButtonMode.Sync
            } else {
                BlinklySyncComponent.ButtonMode.SignIn
            },
        )
    )

    override fun onPrimaryButtonClick() = Unit
    override fun onGoogleSignInCompleted(user: BlinklyUser) = Unit
    override fun onGoogleSignInFailed(throwable: Throwable) = Unit
}
