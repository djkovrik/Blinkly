package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface CalendarWatcher {
    val calendar: Flow<List<Workout>>
}
