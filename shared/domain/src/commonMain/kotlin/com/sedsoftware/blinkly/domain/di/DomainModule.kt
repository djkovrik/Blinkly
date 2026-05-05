package com.sedsoftware.blinkly.domain.di

import com.sedsoftware.blinkly.domain.BlinklyAchievementsWatcher
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyExerciseManager
import com.sedsoftware.blinkly.domain.BlinklyHighlightsProvider
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.impl.BlinklyAchievementsWatcherImpl
import com.sedsoftware.blinkly.domain.impl.BlinklyCalendarWatcherImpl
import com.sedsoftware.blinkly.domain.impl.BlinklyExerciseManagerImpl
import com.sedsoftware.blinkly.domain.impl.BlinklyHighlightsProviderImpl
import com.sedsoftware.blinkly.domain.impl.BlinklyReminderManagerImpl
import com.sedsoftware.blinkly.domain.impl.BlinklyTreeProgressWatcherImpl

interface DomainModule {
    val achievementsWatcher: BlinklyAchievementsWatcher
    val calendarWatcher: BlinklyCalendarWatcher
    val exerciseManager: BlinklyExerciseManager
    val treeProgressWatcher: BlinklyTreeProgressWatcher
    val highlightsProvider: BlinklyHighlightsProvider
    val reminderManager: BlinklyReminderManager
}

interface DomainModuleDependencies {
    val alarmManager: BlinklyAlarmManager
    val database: BlinklyDatabase
    val notifier: BlinklyNotifier
    val settings: BlinklySettings
    val timeUtils: BlinklyTimeUtils
    val dispatchers: BlinklyDispatchers
}

fun DomainModule(dependencies: DomainModuleDependencies): DomainModule {
    return object : DomainModule {
        override val achievementsWatcher: BlinklyAchievementsWatcher by lazy {
            BlinklyAchievementsWatcherImpl(
                database = dependencies.database,
                notifier = dependencies.notifier,
                settings = dependencies.settings,
                timeUtils = dependencies.timeUtils,
                dispatchers = dependencies.dispatchers,
            )
        }

        override val calendarWatcher: BlinklyCalendarWatcher by lazy {
            BlinklyCalendarWatcherImpl(
                database = dependencies.database,
                dispatchers = dependencies.dispatchers,
            )
        }

        override val exerciseManager: BlinklyExerciseManager by lazy {
            BlinklyExerciseManagerImpl(
                database = dependencies.database,
                settings = dependencies.settings,
                timeUtils = dependencies.timeUtils,
                dispatchers = dependencies.dispatchers,
            )
        }

        override val treeProgressWatcher: BlinklyTreeProgressWatcher by lazy {
            BlinklyTreeProgressWatcherImpl(
                timeUtils = dependencies.timeUtils,
                database = dependencies.database,
                dispatchers = dependencies.dispatchers,
            )
        }

        override val highlightsProvider: BlinklyHighlightsProvider by lazy {
            BlinklyHighlightsProviderImpl(
                settings = dependencies.settings,
                timeUtils = dependencies.timeUtils,
                dispatchers = dependencies.dispatchers,
            )
        }

        override val reminderManager: BlinklyReminderManager by lazy {
            BlinklyReminderManagerImpl(
                alarmManager = dependencies.alarmManager,
                database = dependencies.database,
                timeUtils = dependencies.timeUtils,
                dispatchers = dependencies.dispatchers,
            )
        }
    }
}
