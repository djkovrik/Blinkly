package com.sedsoftware.blinkly.alarm

import co.touchlab.kermit.Logger
import com.sedsoftware.blinkly.alarm.di.AlarmModuleDependencies
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.model.ReminderType
import kotlinx.datetime.LocalDateTime

actual fun getBlinklyAlarmManagerImplementation(dependencies: AlarmModuleDependencies): BlinklyAlarmManager =
    object : BlinklyAlarmManager {
        override fun scheduleDaily(uuid: String, type: ReminderType, startingDate: LocalDateTime) {
            Logger.i { "BlinklyAlarmManager->scheduleDaily $uuid $type $startingDate" }
        }

        override fun scheduleWeekly(uuid: String, type: ReminderType, startingDate: LocalDateTime) {
            Logger.i { "BlinklyAlarmManager->scheduleWeekly $uuid $type $startingDate" }
        }

        override fun cancel(uuid: String) {
            Logger.i { "BlinklyAlarmManager->cancel $uuid" }
        }

        override fun cancelAll() {
            Logger.i { "BlinklyAlarmManager->cancelAll" }
        }
    }
