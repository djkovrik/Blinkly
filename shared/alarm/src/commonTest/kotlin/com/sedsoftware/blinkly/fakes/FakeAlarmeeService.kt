package com.sedsoftware.blinkly.fakes

import com.tweener.alarmee.AlarmeeService
import com.tweener.alarmee.LocalNotificationService
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

class FakeAlarmeeService : AlarmeeService {

    private val fakeService: FakeLocalNotificationService = FakeLocalNotificationService()

    override fun initialize(platformConfiguration: AlarmeePlatformConfiguration) {
        // do nothing
    }

    override val local: LocalNotificationService
        get() = fakeService

    fun currentAlarms() = fakeService.currentAlarms()

    fun clear() = fakeService.clearScheduled()
}
