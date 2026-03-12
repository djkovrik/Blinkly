package com.sedsoftware.blinkly.domain.di

import com.sedsoftware.blinkly.domain.AchievementsWatcher
import com.sedsoftware.blinkly.domain.CalendarWatcher
import com.sedsoftware.blinkly.domain.ExerciseManager
import com.sedsoftware.blinkly.domain.TreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.impl.AchievementsWatcherImpl
import com.sedsoftware.blinkly.domain.impl.CalendarWatcherImpl
import com.sedsoftware.blinkly.domain.impl.ExerciseManagerImpl
import com.sedsoftware.blinkly.domain.impl.TreeProgressWatcherImpl

interface DomainModule {
    val achievementsWatcher: AchievementsWatcher
    val calendarWatcher: CalendarWatcher
    val exerciseManager: ExerciseManager
    val treeProgressWatcher: TreeProgressWatcher
}

interface DomainModuleDependencies {
    val database: BlinklyDatabase
    val notifier: BlinklyNotifier
    val settings: BlinklySettings
    val timeUtils: BlinklyTimeUtils
    val dispatchers: BlinklyDispatchers
}

fun DomainModule(dependencies: DomainModuleDependencies): DomainModule {
    return object : DomainModule {
        override val achievementsWatcher: AchievementsWatcher by lazy {
            AchievementsWatcherImpl(
                database = dependencies.database,
                notifier = dependencies.notifier,
                settings = dependencies.settings,
                timeUtils = dependencies.timeUtils,
                dispatchers = dependencies.dispatchers,
            )
        }

        override val calendarWatcher: CalendarWatcher by lazy {
            CalendarWatcherImpl(
                database = dependencies.database,
                dispatchers = dependencies.dispatchers,
            )
        }

        override val exerciseManager: ExerciseManager by lazy {
            ExerciseManagerImpl(
                database = dependencies.database,
                settings = dependencies.settings,
                timeUtils = dependencies.timeUtils,
                dispatchers = dependencies.dispatchers,
            )
        }

        override val treeProgressWatcher: TreeProgressWatcher by lazy {
            TreeProgressWatcherImpl(
                database = dependencies.database,
                timeUtils = dependencies.timeUtils,
                dispatchers = dependencies.dispatchers,
            )
        }
    }
}
