package com.sedsoftware.blinkly.alarm.impl

import android.app.NotificationManager
import androidx.compose.ui.graphics.Color
import com.sedsoftware.blinkly.alarm.R
import com.tweener.alarmee.channel.AlarmeeNotificationChannel
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

internal fun getAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration {
    return AlarmeeAndroidPlatformConfiguration(
        notificationIconResId = R.drawable.ic_reminder,
        notificationIconColor = Color.White,
        useExactScheduling = false,
        notificationChannels = listOf(
            AlarmeeNotificationChannel(
                id = "dailyNewsChannelId",
                name = "Daily news notifications",
                importance = NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    )
}
