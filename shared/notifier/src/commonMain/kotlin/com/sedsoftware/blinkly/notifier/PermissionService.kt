package com.sedsoftware.blinkly.notifier

import com.sedsoftware.blinkly.domain.model.PermissionResult
import dev.icerock.moko.permissions.Permission
import kotlinx.coroutines.flow.Flow

internal interface PermissionService {
    fun events(): Flow<Pair<Permission, PermissionResult>>
    suspend fun request(permission: Permission): PermissionResult
    suspend fun isGranted(permission: Permission): Boolean
}
