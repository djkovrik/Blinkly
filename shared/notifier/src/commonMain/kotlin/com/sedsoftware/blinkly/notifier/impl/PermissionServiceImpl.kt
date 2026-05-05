package com.sedsoftware.blinkly.notifier.impl

import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.notifier.PermissionService
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PermissionServiceImpl(
    private val controller: PermissionsController,
) : PermissionService {

    private val mutex: Mutex = Mutex()
    private val _events: MutableSharedFlow<Pair<Permission, PermissionResult>> = MutableSharedFlow()

    override fun events(): Flow<Pair<Permission, PermissionResult>> {
        return _events
    }

    override suspend fun request(permission: Permission): PermissionResult {
        return mutex.withLock {
            val state = controller.getPermissionState(permission)

            if (state == PermissionState.Granted) {
                val result = PermissionResult.Granted
                _events.emit(permission to result)
                return@withLock result
            }

            val result = try {
                controller.providePermission(permission)
                PermissionResult.Granted

            } catch (_: DeniedAlwaysException) {
                PermissionResult.DeniedAlways

            } catch (_: DeniedException) {
                PermissionResult.Denied
            }

            _events.emit(permission to result)

            result
        }
    }

    override suspend fun isGranted(permission: Permission): Boolean {
        val state = controller.getPermissionState(permission)
        return state == PermissionState.Granted
    }
}
