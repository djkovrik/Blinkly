package com.sedsoftware.blinkly.compose.ui.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.content_description_google_sign_in
import blinkly.shared.compose.generated.resources.content_description_sync
import blinkly.shared.compose.generated.resources.ic_google
import blinkly.shared.compose.generated.resources.ic_sync
import blinkly.shared.compose.generated.resources.sync_button_syncing
import blinkly.shared.compose.generated.resources.sync_button_sync_now
import blinkly.shared.compose.generated.resources.sync_sign_in_google
import blinkly.shared.compose.generated.resources.sync_status_failed
import blinkly.shared.compose.generated.resources.sync_status_last_synced
import blinkly.shared.compose.generated.resources.sync_status_not_synced
import blinkly.shared.compose.generated.resources.sync_status_syncing
import blinkly.shared.compose.generated.resources.sync_title
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mmk.kmpauth.core.KMPAuthInternalApi
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.sedsoftware.blinkly.component.sync.BlinklySyncComponent
import com.sedsoftware.blinkly.component.sync.auth.toBlinklyUser
import com.sedsoftware.blinkly.component.sync.integration.BlinklySyncComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun BlinklySyncContent(
    component: BlinklySyncComponent,
    modifier: Modifier = Modifier,
    enableGoogleSignInContainer: Boolean = true,
    syncTimeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    val model: BlinklySyncComponent.Model by component.model.subscribeAsState()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(space = 4.dp),
                    modifier = Modifier.weight(weight = 1f),
                ) {
                    Text(
                        text = stringResource(resource = Res.string.sync_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = model.statusText(timeZone = syncTimeZone),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (model.buttonMode == BlinklySyncComponent.ButtonMode.SignIn && enableGoogleSignInContainer) {
                GoogleSignInButtonContainer(component, model)
            } else {
                SyncActionButton(
                    model = model,
                    onClick = component::onPrimaryButtonClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(KMPAuthInternalApi::class)
@Composable
private fun GoogleSignInButtonContainer(
    component: BlinklySyncComponent,
    model: BlinklySyncComponent.Model,
) {
    GoogleButtonUiContainerFirebase(
        onResult = { result: Result<FirebaseUser?> ->
            result
                .onSuccess { user ->
                    if (user != null) {
                        component.onGoogleSignInCompleted(user.toBlinklyUser())
                    } else {
                        component.onGoogleSignInFailed(IllegalStateException("Google Sign-In returned no user"))
                    }
                }
                .onFailure(component::onGoogleSignInFailed)
        },
        linkAccount = false,
    ) {
        SyncActionButton(
            model = model,
            onClick = { this.onClick() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SyncActionButton(
    model: BlinklySyncComponent.Model,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSigningIn = model.buttonMode == BlinklySyncComponent.ButtonMode.SignIn
    val isSyncing = model.isSyncing
    val text = when {
        isSyncing -> stringResource(resource = Res.string.sync_button_syncing)
        isSigningIn -> stringResource(resource = Res.string.sync_sign_in_google)
        else -> stringResource(resource = Res.string.sync_button_sync_now)
    }

    Button(
        onClick = onClick,
        enabled = !isSyncing,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        Icon(
            painter = painterResource(
                resource = if (isSigningIn) {
                    Res.drawable.ic_google
                } else {
                    Res.drawable.ic_sync
                }
            ),
            contentDescription = stringResource(
                resource = if (isSigningIn) {
                    Res.string.content_description_google_sign_in
                } else {
                    Res.string.content_description_sync
                }
            ),
            modifier = if (isSigningIn) {
                Modifier.size(size = 30.dp)
            } else {
                Modifier.size(size = 22.dp)
            },
            tint = if (isSigningIn) {
                Color.Unspecified
            } else {
                MaterialTheme.colorScheme.onPrimary
            },
        )

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
@OptIn(ExperimentalTime::class)
private fun BlinklySyncComponent.Model.statusText(timeZone: TimeZone): String =
    when (val currentStatus = status) {
        BlinklySyncComponent.Status.NotSynced ->
            stringResource(resource = Res.string.sync_status_not_synced)

        BlinklySyncComponent.Status.Syncing ->
            stringResource(resource = Res.string.sync_status_syncing)

        is BlinklySyncComponent.Status.Synced ->
            stringResource(
                resource = Res.string.sync_status_last_synced,
                currentStatus.at.asSyncDate(timeZone = timeZone),
            )

        is BlinklySyncComponent.Status.Failed ->
            stringResource(resource = Res.string.sync_status_failed)
    }

@OptIn(ExperimentalTime::class)
internal fun Instant.asSyncDate(timeZone: TimeZone): String =
    toLocalDateTime(timeZone)
        .toString()
        .substringBefore(".")
        .replace(oldChar = 'T', newChar = ' ')

@Preview(widthDp = 420, heightDp = 760)
@Composable
private fun BlinklySyncContentPreviewLight() {
    BlinklyWidgetPreview {
        BlinklySyncContentPreviewBoard()
    }
}

@Preview(widthDp = 420, heightDp = 760, uiMode = 32)
@Composable
private fun BlinklySyncContentPreviewDark() {
    BlinklyWidgetPreview(isDarkTheme = true) {
        BlinklySyncContentPreviewBoard()
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun BlinklySyncContentPreviewBoard(
    syncTimeZone: TimeZone = TimeZone.UTC,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 12.dp),
        modifier = Modifier.padding(all = 16.dp),
    ) {
        BlinklySyncContent(
            component = BlinklySyncComponentPreview(),
            enableGoogleSignInContainer = false,
            syncTimeZone = syncTimeZone,
        )

        BlinklySyncContent(
            component = BlinklySyncComponentPreview(isAuthorized = true),
            enableGoogleSignInContainer = false,
            syncTimeZone = syncTimeZone,
        )

        BlinklySyncContent(
            component = BlinklySyncComponentPreview(
                isAuthorized = true,
                isSyncing = true,
            ),
            enableGoogleSignInContainer = false,
            syncTimeZone = syncTimeZone,
        )

        BlinklySyncContent(
            component = BlinklySyncComponentPreview(
                isAuthorized = true,
                lastSyncedAt = Instant.fromEpochMilliseconds(epochMilliseconds = 1_725_369_600_000L),
            ),
            enableGoogleSignInContainer = false,
            syncTimeZone = syncTimeZone,
        )

        BlinklySyncContent(
            component = BlinklySyncComponentPreview(
                isAuthorized = true,
                status = BlinklySyncComponent.Status.Failed(message = null),
            ),
            enableGoogleSignInContainer = false,
            syncTimeZone = syncTimeZone,
        )
    }
}
