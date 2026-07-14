package com.sedsoftware.blinkly.alarm.impl

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.sedsoftware.blinkly.domain.external.BlinklyExactAlarmPermissionController

class BlinklyExactAlarmPermissionControllerAndroid(
    context: Context,
) : BlinklyExactAlarmPermissionController {

    private val applicationContext: Context = context.applicationContext

    override fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        return alarmManager?.canScheduleExactAlarms() == true
    }

    override fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || canScheduleExactAlarms()) return

        val intent = Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${applicationContext.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        applicationContext.startActivity(intent)
    }
}
