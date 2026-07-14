package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.impl.BlinklyReminderManagerImpl

fun createBlinklyReminderManager(
    alarmManager: BlinklyAlarmManager,
    database: BlinklyDatabase,
    timeUtils: BlinklyTimeUtils,
    dispatchers: BlinklyDispatchers,
): BlinklyReminderManager =
    BlinklyReminderManagerImpl(
        alarmManager = alarmManager,
        database = database,
        timeUtils = timeUtils,
        dispatchers = dispatchers,
    )
