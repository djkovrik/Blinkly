package com.sedsoftware.blinkly.notifier

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.notifier.impl.BlinklyNotifierImpl
import com.sedsoftware.blinkly.notifier.impl.PermissionServiceImpl
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import dev.icerock.moko.permissions.test.createPermissionControllerMock
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class BlinklyNotifierTest {

    private val testScheduler: TestCoroutineScheduler = TestCoroutineScheduler()
    private val dispatcher: TestDispatcher = StandardTestDispatcher(testScheduler)
    private val controller: PermissionsController = createPermissionControllerMock(setOf(Permission.REMOTE_NOTIFICATION))
    private val service: PermissionService = PermissionServiceImpl(controller)
    private val notifier: BlinklyNotifier = BlinklyNotifierImpl(service)

    @Test
    fun `when achievementUnlocked then emit flow event`() = runTest(testScheduler) {
        // given
        val type = AchievementType.TWENTY_X3
        val achievements = mutableListOf<AchievementType>()
        // when
        val collectionJob = launch(dispatcher) { notifier.unlockedAchievements().collect { achievements.add(it) } }
        testScheduler.advanceUntilIdle()

        notifier.achievementUnlocked(type)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(achievements).contains(type)
        collectionJob.cancel()
    }

    @Test
    fun `when permission granted then isNotificationPermissionGranted returns true`() = runTest(testScheduler) {
        // given
        // when
        notifier.requestNotificationPermission()
        testScheduler.advanceUntilIdle()
        val granted = notifier.isNotificationPermissionGranted()
        // then
        assertThat(granted).isTrue()
    }

    @Test
    fun `when permission granted then emit PermissionResult`() = runTest(testScheduler) {
        // given
        val events = mutableListOf<PermissionResult>()
        // when
        val collectionJob = launch(dispatcher) { notifier.permissionEvents().collect { events.add(it) } }
        testScheduler.advanceUntilIdle()

        notifier.requestNotificationPermission()
        testScheduler.advanceUntilIdle()
        // then
        assertThat(events).contains(PermissionResult.Granted)
        collectionJob.cancel()
    }
}
