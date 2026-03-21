package com.sedsoftware.blinkly.alarm.impl

import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.tweener.alarmee.AlarmeeService
import com.tweener.alarmee.LocalNotificationService
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.createAlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationConfiguration
import com.tweener.alarmee.model.AndroidNotificationPriority
import com.tweener.alarmee.model.IosNotificationConfiguration
import com.tweener.alarmee.model.RepeatInterval
import kotlinx.datetime.LocalDateTime

internal class BlinklyAlarmManagerImpl(
    val timeUtils: BlinklyTimeUtils,
    val notificationConfigurations: Map<ReminderType, ReminderConfig>,
    val platformConfiguration: AlarmeePlatformConfiguration = getAlarmeePlatformConfiguration(),
    val alarmeeService: AlarmeeService = createAlarmeeService(),
) : BlinklyAlarmManager {

    private val service: LocalNotificationService by lazy {
        alarmeeService.initialize(platformConfiguration)
        alarmeeService.local
    }

    override fun scheduleDaily(uuid: String, type: ReminderType, startingDate: LocalDateTime) {
        schedule(uuid = uuid, type = type, startingDate = startingDate, interval = RepeatInterval.Daily)
    }

    override fun scheduleWeekly(uuid: String, type: ReminderType, startingDate: LocalDateTime) {
        schedule(uuid = uuid, type = type, startingDate = startingDate, interval = RepeatInterval.Weekly)
    }

    override fun cancel(uuid: String) {
        service.cancel(uuid = uuid)
    }

    override fun cancelAll() {
        service.cancelAll()
    }

    private fun schedule(uuid: String, type: ReminderType, startingDate: LocalDateTime, interval: RepeatInterval) {
        val title: String = notificationConfigurations[type]?.title.orEmpty()
        val description: String = notificationConfigurations[type]?.description.orEmpty()

        service.schedule(
            alarmee = Alarmee(
                uuid = uuid,
                notificationTitle = title,
                notificationBody = description,
                scheduledDateTime = startingDate,
                timeZone = timeUtils.timeZone(),
                repeatInterval = interval,
                androidNotificationConfiguration = AndroidNotificationConfiguration(
                    priority = AndroidNotificationPriority.MAXIMUM,
                    channelId = BLINKLY_CHANNEL_ID,
                ),
                iosNotificationConfiguration = IosNotificationConfiguration(),
            )
        )
    }

    private companion object {
        const val BLINKLY_CHANNEL_ID = "BlinklyReminders"
    }
}
