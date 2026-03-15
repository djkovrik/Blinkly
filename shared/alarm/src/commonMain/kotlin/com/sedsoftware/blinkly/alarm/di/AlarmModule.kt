package com.sedsoftware.blinkly.alarm.di

import com.sedsoftware.blinkly.alarm.impl.BlinklyAlarmManagerImpl
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

interface AlarmModule {
    val alarmManager: BlinklyAlarmManager
}

interface AlarmModuleDependencies {
    val contentConfigurations: Map<ReminderType, ReminderConfig>
    val platformConfiguration: AlarmeePlatformConfiguration
}

fun AlarmModule(dependencies: AlarmModuleDependencies): AlarmModule {
    return object : AlarmModule {
        override val alarmManager: BlinklyAlarmManager by lazy {
            BlinklyAlarmManagerImpl(
                notificationConfigurations = dependencies.contentConfigurations,
                platformConfiguration = dependencies.platformConfiguration,
            )
        }
    }
}
