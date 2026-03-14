package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.PermissionResult
import kotlinx.coroutines.flow.Flow

interface BlinklyNotifier {
    fun unlockedAchievements(): Flow<AchievementType>
    fun permissionEvents(): Flow<PermissionResult>
    suspend fun isNotificationPermissionGranted(): Boolean
    suspend fun requestNotificationPermission()
    suspend fun achievementUnlocked(type: AchievementType)
}
