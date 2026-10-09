package com.sedsoftware.blinkly.alarm.impl

import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.AppComponentFactory
import com.tweener.alarmee.reveicer.NotificationBroadcastReceiver

/** Keeps Alarmee's PendingIntent identity, including alarms created before an app update. */
@RequiresApi(Build.VERSION_CODES.P)
class BlinklyAlarmComponentFactory : AppComponentFactory() {

    override fun instantiateReceiverCompat(
        cl: ClassLoader,
        className: String,
        intent: Intent?,
    ): BroadcastReceiver =
        if (className == NotificationBroadcastReceiver::class.java.name) {
            BlinklyNotificationReceiver()
        } else {
            super.instantiateReceiverCompat(cl, className, intent)
        }
}
