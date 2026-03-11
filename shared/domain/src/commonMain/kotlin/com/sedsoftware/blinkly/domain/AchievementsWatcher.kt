package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementsWatcher {
    val achievements: Flow<List<Achievement>>
}
