package com.sedsoftware.blinkly.component.sync

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.sync.integration.BlinklySyncComponentDefault
import com.sedsoftware.blinkly.domain.external.BlinklySyncManager
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.BlinklyAuthSession
import com.sedsoftware.blinkly.domain.model.BlinklySyncState
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Instant

class BlinklySyncComponentTest : ComponentTest<BlinklySyncComponent>() {

    private val user = BlinklyUser(id = "user", displayName = null, email = null)
    private val syncManager: FakeBlinklySyncManager = FakeBlinklySyncManager()

    @Test
    fun `when auth is restoring then model disables sign in`() = runTest(testScheduler) {
        testScheduler.advanceUntilIdle()

        assertThat(component.model.value.buttonMode).isEqualTo(BlinklySyncComponent.ButtonMode.Restoring)
        assertThat(component.model.value.status).isEqualTo(BlinklySyncComponent.Status.Restoring)
    }

    @Test
    fun `when restoring resolves signed out then model asks to sign in`() = runTest(testScheduler) {
        syncManager.emit(authSession = BlinklyAuthSession.SignedOut)
        testScheduler.advanceUntilIdle()

        assertThat(component.model.value.buttonMode).isEqualTo(BlinklySyncComponent.ButtonMode.SignIn)
        assertThat(component.model.value.status).isEqualTo(BlinklySyncComponent.Status.NotSynced)
    }

    @Test
    fun `when authorized without last sync then model asks to sync`() = runTest(testScheduler) {
        // given
        testScheduler.advanceUntilIdle()

        // when
        syncManager.emit(authSession = BlinklyAuthSession.SignedIn(user), lastSyncedAt = null)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.buttonMode).isEqualTo(BlinklySyncComponent.ButtonMode.Sync)
        assertThat(component.model.value.status).isEqualTo(BlinklySyncComponent.Status.NotSynced)
    }

    @Test
    fun `when component is reopened with authorized state then it immediately asks to sync`() = runTest(testScheduler) {
        syncManager.emit(authSession = BlinklyAuthSession.SignedIn(user), lastSyncedAt = null)

        val reopenedComponent = createSyncComponent(syncManager)

        assertThat(reopenedComponent.model.value.buttonMode).isEqualTo(BlinklySyncComponent.ButtonMode.Sync)
    }

    @Test
    fun `when authorized with last sync then model shows synced status`() = runTest(testScheduler) {
        // given
        val syncedAt = Instant.fromEpochMilliseconds(1_000)
        testScheduler.advanceUntilIdle()

        // when
        syncManager.emit(authSession = BlinklyAuthSession.SignedIn(user), lastSyncedAt = syncedAt)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.status).isEqualTo(BlinklySyncComponent.Status.Synced(syncedAt))
    }

    @Test
    fun `when Google sign in succeeds then sync is started`() = runTest(testScheduler) {
        // given
        val syncedAt = Instant.fromEpochMilliseconds(2_000)
        syncManager.nextSyncAt = syncedAt
        testScheduler.advanceUntilIdle()

        // when
        component.onGoogleSignInCompleted(BlinklyUser(id = "user", displayName = null, email = null))
        testScheduler.advanceUntilIdle()

        // then
        assertThat(syncManager.completeGoogleSignInCalls).isEqualTo(1)
        assertThat(component.model.value.status).isEqualTo(BlinklySyncComponent.Status.Synced(syncedAt))
    }

    @Test
    fun `when Google sign in fails then component publishes auth error`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("cancelled")
        testScheduler.advanceUntilIdle()

        // when
        component.onGoogleSignInFailed(exception)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(componentOutputContainsErrorCausedBy<BlinklyError.SyncAuthFailed>(exception)).isTrue()
        assertThat(component.model.value.isSyncing).isEqualTo(false)
    }

    @Test
    fun `when authorized primary clicked then sync now is called`() = runTest(testScheduler) {
        // given
        val syncedAt = Instant.fromEpochMilliseconds(3_000)
        syncManager.nextSyncAt = syncedAt
        testScheduler.advanceUntilIdle()
        syncManager.emit(authSession = BlinklyAuthSession.SignedIn(user))
        testScheduler.advanceUntilIdle()

        // when
        component.onPrimaryButtonClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(syncManager.syncNowCalls).isEqualTo(1)
        assertThat(component.model.value.status).isEqualTo(BlinklySyncComponent.Status.Synced(syncedAt))
    }

    @Test
    fun `when sync fails then component shows failed status and publishes error`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("sync failed")
        syncManager.nextSyncFailure = exception
        testScheduler.advanceUntilIdle()
        syncManager.emit(authSession = BlinklyAuthSession.SignedIn(user))
        testScheduler.advanceUntilIdle()

        // when
        component.onPrimaryButtonClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(componentOutputContainsErrorCausedBy<BlinklyError.SyncUnknown>(exception)).isTrue()
        assertThat(component.model.value.isSyncing).isEqualTo(false)
    }

    override fun createComponent(): BlinklySyncComponent =
        createSyncComponent(syncManager)

    private fun createSyncComponent(syncManager: BlinklySyncManager): BlinklySyncComponent =
        BlinklySyncComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            syncManager = syncManager,
            syncOutput = { componentOutput.add(it) },
        )

    private class FakeBlinklySyncManager : BlinklySyncManager {
        private val stateFlow: MutableStateFlow<BlinklySyncState> = MutableStateFlow(
            BlinklySyncState(
                authSession = BlinklyAuthSession.Restoring,
                isSyncing = false,
                lastSyncedAt = null,
                error = null,
            )
        )

        var syncNowCalls: Int = 0
        var completeGoogleSignInCalls: Int = 0
        var nextSyncAt: Instant = Instant.fromEpochMilliseconds(1)
        var nextSyncFailure: Throwable? = null

        override val state: StateFlow<BlinklySyncState> = stateFlow

        override suspend fun completeGoogleSignIn(user: BlinklyUser) {
            completeGoogleSignInCalls += 1
            sync(successUser = user)
        }

        override suspend fun syncNow() {
            syncNowCalls += 1
            sync(successUser = null)
        }

        fun emit(
            authSession: BlinklyAuthSession,
            isSyncing: Boolean = false,
            lastSyncedAt: Instant? = null,
            error: BlinklyError? = null,
        ) {
            stateFlow.value = BlinklySyncState(
                authSession = authSession,
                isSyncing = isSyncing,
                lastSyncedAt = lastSyncedAt,
                error = error,
            )
        }

        private fun sync(successUser: BlinklyUser?) {
            val failure = nextSyncFailure
            stateFlow.value = stateFlow.value.copy(
                authSession = successUser?.let(BlinklyAuthSession::SignedIn) ?: stateFlow.value.authSession,
                isSyncing = true,
                error = null,
            )

            if (failure != null) {
                stateFlow.value = stateFlow.value.copy(isSyncing = false)
                throw failure
            }

            stateFlow.value = stateFlow.value.copy(
                authSession = BlinklyAuthSession.SignedIn(
                    successUser ?: (stateFlow.value.authSession as BlinklyAuthSession.SignedIn).user
                ),
                isSyncing = false,
                lastSyncedAt = nextSyncAt,
                error = null,
            )
        }

    }
}
