package com.sedsoftware.blinkly.alarm.impl

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import co.touchlab.kermit.Logger
import com.tweener.alarmee.notification.NotificationFactory
import com.tweener.alarmee.reveicer.NotificationBroadcastReceiver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles only Blinkly's local reminder payloads, using Alarmee 2.7's intent contract. */
internal class BlinklyNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationBroadcastReceiver.ALARM_ACTION) return
        val uuid = intent.getStringExtra(NotificationBroadcastReceiver.KEY_UUID)
        val title = intent.getStringExtra(NotificationBroadcastReceiver.KEY_TITLE)
        val body = intent.getStringExtra(NotificationBroadcastReceiver.KEY_BODY)
        if (uuid == null || title == null || body == null) return
        val pendingResult = goAsync()
        val applicationContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                deliverReminder(
                    scheduleNext = { scheduleNext(applicationContext, intent, uuid) },
                    showNotification = { showNotification(applicationContext, intent, uuid, title, body) },
                    onFailure = { Logger.w { "Android reminder delivery failed (${it.javaClass.simpleName})" } },
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun showNotification(context: Context, intent: Intent, uuid: String, title: String, body: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (!manager.areNotificationsEnabled()) return
        val notification = NotificationFactory.create(
            context = context,
            channelId = intent.getStringExtra(NotificationBroadcastReceiver.KEY_CHANNEL_ID) ?: "notificationsChannelId",
            title = title,
            body = body,
            priority = intent.getIntExtra(NotificationBroadcastReceiver.KEY_PRIORITY, 0),
            iconResId = intent.getIntExtra(
                NotificationBroadcastReceiver.KEY_ICON_RES_ID,
                NotificationBroadcastReceiver.DEFAULT_ICON_RES_ID,
            ),
            iconColor = intent.getIntExtra(NotificationBroadcastReceiver.KEY_ICON_COLOR, 0),
            soundFilename = intent.getStringExtra(NotificationBroadcastReceiver.KEY_SOUND_FILENAME),
            notificationUuid = uuid,
        )
        manager.notify(uuid.hashCode(), notification)
    }

    private fun scheduleNext(context: Context, intent: Intent, uuid: String) {
        val interval = intent.getLongExtra(NotificationBroadcastReceiver.KEY_REPEAT_INTERVAL_MILLIS, -1L)
        if (interval <= 0) return
        val now = System.currentTimeMillis()
        val anchor = intent.getLongExtra(NotificationBroadcastReceiver.KEY_NEXT_TRIGGER_MILLIS, -1L)
        val trigger = nextReminderTrigger(now, anchor, interval)
        val nextIntent = Intent(intent).apply {
            // Keep the original library component so scheduling and cancellation use the same identity.
            setClass(context, NotificationBroadcastReceiver::class.java)
            putExtra(NotificationBroadcastReceiver.KEY_NEXT_TRIGGER_MILLIS, trigger + interval)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uuid.hashCode(),
            nextIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
        } else {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
        }
    }
}

internal fun nextReminderTrigger(now: Long, anchor: Long, interval: Long): Long {
    val next = if (anchor > 0) anchor else now + interval
    return if (next > now) next else next + ((now - next) / interval + 1) * interval
}

/** Isolate native scheduling and notification failures; never swallow coroutine cancellation. */
internal suspend fun deliverReminder(
    scheduleNext: () -> Unit,
    showNotification: suspend () -> Unit,
    onFailure: (Exception) -> Unit,
) {
    attemptReminderOperation(onFailure) { scheduleNext() }
    attemptReminderOperation(onFailure) { showNotification() }
}

@Suppress("TooGenericExceptionCaught")
private suspend fun attemptReminderOperation(onFailure: (Exception) -> Unit, operation: suspend () -> Unit) {
    try {
        operation()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (exception: Exception) {
        // OEM notification services can throw arbitrary RuntimeExceptions through Binder.
        onFailure(exception)
    }
}
