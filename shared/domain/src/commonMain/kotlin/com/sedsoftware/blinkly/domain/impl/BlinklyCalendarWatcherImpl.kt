package com.sedsoftware.blinkly.domain.impl

import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn

internal class BlinklyCalendarWatcherImpl(
    database: BlinklyDatabase,
    dispatchers: BlinklyDispatchers,
) : BlinklyCalendarWatcher {

    private val scope: CoroutineScope = CoroutineScope(dispatchers.io + SupervisorJob())

    private val calendarFlow: Flow<List<Workout>> = database.currentCalendar()
        .flowOn(dispatchers.io)
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = SUBSCRIPTION_STOP_TIMEOUT),
            replay = 1,
        )

    override val calendar: Flow<List<Workout>>
        get() = calendarFlow

    private companion object {
        const val SUBSCRIPTION_STOP_TIMEOUT = 5000L
    }
}
