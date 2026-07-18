package com.sedsoftware.blinkly.compose.ui.achievements

import com.sedsoftware.blinkly.domain.model.AchievementLevel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AchievementsAdPlacementTest {

    @Test
    fun `ad is placed after beginner level regardless of section index`() {
        assertTrue(shouldPlaceAchievementsAdAfter(AchievementLevel.BEGINNER))
        assertFalse(shouldPlaceAchievementsAdAfter(AchievementLevel.INTERMEDIATE))
        assertFalse(shouldPlaceAchievementsAdAfter(AchievementLevel.PRO))
    }
}
