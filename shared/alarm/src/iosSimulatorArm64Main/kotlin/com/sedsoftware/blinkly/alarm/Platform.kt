package com.sedsoftware.blinkly.alarm

import com.sedsoftware.blinkly.alarm.di.AlarmModuleDependencies
import com.sedsoftware.blinkly.alarm.impl.BlinklyAlarmManagerImpl
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager

actual fun getBlinklyAlarmManagerImplementation(dependencies: AlarmModuleDependencies): BlinklyAlarmManager =
    BlinklyAlarmManagerImpl(
        timeUtils = dependencies.timeUtils,
        notificationConfigurations = dependencies.contentConfigurations,
    )
