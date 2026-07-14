package com.sedsoftware.blinkly.component.root

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.sedsoftware.blinkly.alarm.di.AlarmModule
import com.sedsoftware.blinkly.alarm.di.AlarmModuleDependencies
import com.sedsoftware.blinkly.alarm.impl.BlinklyExactAlarmPermissionControllerAndroid
import com.sedsoftware.blinkly.database.BlinklyDatabaseDriverFactory
import com.sedsoftware.blinkly.database.di.DatabaseModule
import com.sedsoftware.blinkly.database.di.DatabaseModuleDependencies
import com.sedsoftware.blinkly.domain.createBlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.utils.di.UtilsModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

suspend fun rescheduleBlinklyReminders(
    context: Context,
    contentConfigurations: Map<ReminderType, ReminderConfig>,
): Boolean {
    val applicationContext = context.applicationContext
    val dispatchers = object : BlinklyDispatchers {
        override val main: CoroutineDispatcher = Dispatchers.Main.immediate
        override val io: CoroutineDispatcher = Dispatchers.IO
    }
    val timeUtils = UtilsModule().timeUtils
    val alarmManager = AlarmModule(
        dependencies = object : AlarmModuleDependencies {
            override val timeUtils: BlinklyTimeUtils = timeUtils
            override val contentConfigurations: Map<ReminderType, ReminderConfig> = contentConfigurations
            override val exactAlarmPermissionController =
                BlinklyExactAlarmPermissionControllerAndroid(applicationContext)
        }
    ).alarmManager

    if (!alarmManager.canScheduleExactAlarms()) return false

    val sqlDriver = BlinklyDatabaseDriverFactory(applicationContext)
    sqlDriver.use { sqlDriver ->
        val database = DatabaseModule(
            dependencies = object : DatabaseModuleDependencies {
                override val driver: SqlDriver = sqlDriver
                override val dispatchers: BlinklyDispatchers = dispatchers
                override val timeUtils: BlinklyTimeUtils = timeUtils
            }
        ).database
        val reminderManager = createBlinklyReminderManager(
            alarmManager = alarmManager,
            database = database,
            timeUtils = timeUtils,
            dispatchers = dispatchers,
        )

        reminderManager.rescheduleAll()
    }
    return true
}
