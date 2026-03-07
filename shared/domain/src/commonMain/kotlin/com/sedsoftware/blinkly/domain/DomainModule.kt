package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.internal.ExerciseProgressWatcherImpl

interface DomainModule {
    val exerciseProgressWatcher: ExerciseProgressWatcher
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
        override val exerciseProgressWatcher: ExerciseProgressWatcher by lazy {
            ExerciseProgressWatcherImpl(
                database = dependencies.database,
                notifier = dependencies.notifier,
                settings = dependencies.settings,
                timeUtils = dependencies.timeUtils,
                dispatchers = dependencies.dispatchers,
            )
        }
    }
}
