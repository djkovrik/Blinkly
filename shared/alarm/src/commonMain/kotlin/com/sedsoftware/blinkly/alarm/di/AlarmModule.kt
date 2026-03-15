package com.sedsoftware.blinkly.alarm.di

import com.sedsoftware.blinkly.alarm.impl.BlinklyAlarmManagerImpl
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

interface AlarmModule {
    val alarmManager: BlinklyAlarmManager
}

interface AlarmModuleDependencies {
    val timeUtils: BlinklyTimeUtils
    val contentConfigurations: Map<ReminderType, ReminderConfig>
    val platformConfiguration: AlarmeePlatformConfiguration
}

fun AlarmModule(dependencies: AlarmModuleDependencies): AlarmModule {
    return object : AlarmModule {
        override val alarmManager: BlinklyAlarmManager by lazy {
            BlinklyAlarmManagerImpl(
                timeUtils = dependencies.timeUtils,
                notificationConfigurations = dependencies.contentConfigurations,
                platformConfiguration = dependencies.platformConfiguration,
            )
        }
    }
}
