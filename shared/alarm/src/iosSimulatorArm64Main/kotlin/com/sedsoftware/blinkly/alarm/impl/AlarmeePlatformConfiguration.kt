package com.sedsoftware.blinkly.alarm.impl

import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

internal fun getAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration {
    return AlarmeeIosPlatformConfiguration
}
