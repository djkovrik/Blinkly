package com.sedsoftware.blinkly.domain.external

interface BlinklyExactAlarmPermissionController {
    fun canScheduleExactAlarms(): Boolean = true
    fun requestExactAlarmPermission() = Unit

    data object Granted : BlinklyExactAlarmPermissionController
}
