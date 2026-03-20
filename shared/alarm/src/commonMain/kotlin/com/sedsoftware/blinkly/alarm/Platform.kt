package com.sedsoftware.blinkly.alarm

import com.sedsoftware.blinkly.alarm.di.AlarmModuleDependencies
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager

expect fun getBlinklyAlarmManagerImplementation(dependencies: AlarmModuleDependencies): BlinklyAlarmManager
