package com.sedsoftware.blinkly.domain.model

sealed interface PermissionResult {
    data object Granted : PermissionResult
    data object Denied : PermissionResult
    data object DeniedAlways : PermissionResult
}
