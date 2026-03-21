package com.sedsoftware.blinkly.alarm.di

import com.sedsoftware.blinkly.alarm.getBlinklyAlarmManagerImplementation
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType

interface AlarmModule {
    val alarmManager: BlinklyAlarmManager
}

interface AlarmModuleDependencies {
    val timeUtils: BlinklyTimeUtils
    val contentConfigurations: Map<ReminderType, ReminderConfig>
}

fun AlarmModule(dependencies: AlarmModuleDependencies): AlarmModule {
    return object : AlarmModule {
        override val alarmManager: BlinklyAlarmManager by lazy {
            getBlinklyAlarmManagerImplementation(dependencies)
        }
    }
}
