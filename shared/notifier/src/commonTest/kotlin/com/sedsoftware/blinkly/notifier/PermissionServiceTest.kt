package com.sedsoftware.blinkly.notifier

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.notifier.impl.PermissionServiceImpl
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import dev.icerock.moko.permissions.test.PermissionsControllerMock
import dev.icerock.moko.permissions.test.createPermissionControllerMock
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PermissionServiceTest {

    private val testScheduler: TestCoroutineScheduler = TestCoroutineScheduler()
    private val dispatcher: TestDispatcher = StandardTestDispatcher(testScheduler)
    private val controllerGranted = createPermissionControllerMock(setOf(Permission.REMOTE_NOTIFICATION))
    private val controllerDenied = createPermissionControllerMock()

    @Test
    fun `when permission granted then should publish Granted event`() = runTest(testScheduler) {
        // given
        val service = PermissionServiceImpl(controllerGranted)
        val events: MutableList<PermissionResult> = mutableListOf()
        // when
        val collectionJob = launch(dispatcher) { service.events().collect { events.add(it.second) } }
        testScheduler.advanceUntilIdle()

        service.request(Permission.REMOTE_NOTIFICATION)
        testScheduler.advanceUntilIdle()
        // then
        assertThat(events).contains(PermissionResult.Granted)
        // when
        val granted = service.isGranted(Permission.REMOTE_NOTIFICATION)
        // then
        assertThat(granted).isTrue()
        collectionJob.cancel()
    }

    @Test
    fun `when permission denied then should publish Denied event`() = runTest(testScheduler) {
        // given
        val service = PermissionServiceImpl(controllerDenied)
        val events: MutableList<PermissionResult> = mutableListOf()
        // when
        val collectionJob = launch(dispatcher) { service.events().collect { events.add(it.second) } }
        testScheduler.advanceUntilIdle()

        service.request(Permission.REMOTE_NOTIFICATION)
        testScheduler.advanceUntilIdle()
        // then
        assertThat(events).contains(PermissionResult.Denied)
        // when
        val granted = service.isGranted(Permission.REMOTE_NOTIFICATION)
        // then
        assertThat(granted).isFalse()
        collectionJob.cancel()
    }

    @Test
    fun `when permission denied always then should publish DeniedAlways event`() = runTest(testScheduler) {
        // given
        val service = PermissionServiceImpl(controllerDeniedAlways)
        val events: MutableList<PermissionResult> = mutableListOf()

        // when
        val collectionJob = launch(dispatcher) { service.events().collect { events.add(it.second) } }
        testScheduler.advanceUntilIdle()

        val result = service.request(Permission.REMOTE_NOTIFICATION)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(result).isEqualTo(PermissionResult.DeniedAlways)
        assertThat(events).contains(PermissionResult.DeniedAlways)
        collectionJob.cancel()
    }

    private val controllerDeniedAlways: PermissionsControllerMock =
        object : PermissionsControllerMock() {
            override suspend fun providePermission(permission: Permission) {
                throw DeniedAlwaysException(permission, "mock block permission permanently")
            }

            override suspend fun isPermissionGranted(permission: Permission): Boolean = false

            override suspend fun getPermissionState(permission: Permission): PermissionState = PermissionState.NotDetermined

            override fun openAppSettings() = Unit
        }
}
