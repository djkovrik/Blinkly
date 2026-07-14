package com.sedsoftware.blinkly

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.touchlab.kermit.Logger
import com.sedsoftware.blinkly.component.root.rescheduleBlinklyReminders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BlinklyReminderRescheduleReceiver : BroadcastReceiver() {

    @Suppress("TooGenericExceptionCaught")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in SUPPORTED_ACTIONS) return

        val pendingResult = goAsync()
        val applicationContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val rescheduled = rescheduleBlinklyReminders(
                    context = applicationContext,
                    contentConfigurations = applicationContext.getNotificationConfigurations(),
                )
                if (rescheduled) {
                    Logger.d { "Reminder alarms rescheduled after $action" }
                } else {
                    Logger.w { "Reminder alarms were not rescheduled after $action: exact alarm access is missing" }
                }
            } catch (throwable: Throwable) {
                Logger.e(throwable) { "Unable to reschedule reminder alarms after $action" }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val SUPPORTED_ACTIONS: Set<String> = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )

        const val ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED =
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
    }
}
