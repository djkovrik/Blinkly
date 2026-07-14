package com.sedsoftware.blinkly.alarm.impl

import android.app.NotificationManager
import androidx.compose.ui.graphics.Color
import com.sedsoftware.blinkly.alarm.NotificationConstants
import com.sedsoftware.blinkly.alarm.R
import com.tweener.alarmee.channel.AlarmeeNotificationChannel
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

internal fun getAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration {
    return AlarmeeAndroidPlatformConfiguration(
        notificationIconResId = R.drawable.ic_reminder,
        notificationIconColor = Color.White,
        useExactScheduling = true,
        notificationChannels = listOf(
            AlarmeeNotificationChannel(
                id = NotificationConstants.CHANNEL_ID,
                name = NotificationConstants.CHANNEL_DESCRIPTION,
                soundFilename = "ding",
                importance = NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    )
}
