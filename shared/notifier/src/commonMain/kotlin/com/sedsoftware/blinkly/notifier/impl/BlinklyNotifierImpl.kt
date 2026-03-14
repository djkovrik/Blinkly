package com.sedsoftware.blinkly.notifier.impl

import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.notifier.PermissionService
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

internal class BlinklyNotifierImpl(
    private val permissionsService: PermissionService,
) : BlinklyNotifier {

    private val permission: Permission = Permission.REMOTE_NOTIFICATION
    private val _achievementsFlow: MutableSharedFlow<AchievementType> = MutableSharedFlow()

    override fun unlockedAchievements(): Flow<AchievementType> {
        return _achievementsFlow
    }

    override fun permissionEvents(): Flow<PermissionResult> {
        return permissionsService.events()
            .filter { it.first == permission }
            .map { it.second }
    }

    override suspend fun isNotificationPermissionGranted(): Boolean {
        return permissionsService.isGranted(permission)
    }

    override suspend fun requestNotificationPermission() {
        permissionsService.request(permission)
    }

    override suspend fun achievementUnlocked(type: AchievementType) {
        _achievementsFlow.emit(type)
    }
}
