package com.sedsoftware.blinkly.domain.extension

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.fakes.FakeData
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Clock

class ModelExtTest {

    @Test
    fun `getLevel should return correct level`() = runTest {
        // given
        val levelsMap = mapOf(
            AchievementType.FIRST_SPARK to AchievementLevel.BEGINNER,
            AchievementType.BLINK_STARTER to AchievementLevel.BEGINNER,
            AchievementType.CLOSE_UP to AchievementLevel.BEGINNER,
            AchievementType.CLOCKWISE to AchievementLevel.BEGINNER,
            AchievementType.EVENING_NEWBIE to AchievementLevel.BEGINNER,
            AchievementType.TWENTY_X3 to AchievementLevel.BEGINNER,
            AchievementType.THREE_DAY_STREAK to AchievementLevel.BEGINNER,
            AchievementType.DIAMOND_EYES to AchievementLevel.INTERMEDIATE,
            AchievementType.BLINK_MASTER to AchievementLevel.INTERMEDIATE,
            AchievementType.FAR_SIGHTED to AchievementLevel.INTERMEDIATE,
            AchievementType.THE_ARTIST to AchievementLevel.INTERMEDIATE,
            AchievementType.CLOCKMAKER to AchievementLevel.INTERMEDIATE,
            AchievementType.REVERSE_MYOPE to AchievementLevel.INTERMEDIATE,
            AchievementType.RELAX_ENTHUSIAST to AchievementLevel.INTERMEDIATE,
            AchievementType.REGULAR_WARM_UP to AchievementLevel.INTERMEDIATE,
            AchievementType.EVENING_RITUAL to AchievementLevel.INTERMEDIATE,
            AchievementType.EXPRESS_MASTER to AchievementLevel.INTERMEDIATE,
            AchievementType.THIRTY_EXERCISES to AchievementLevel.INTERMEDIATE,
            AchievementType.HUNDRED_EXERCISES to AchievementLevel.INTERMEDIATE,
            AchievementType.IRON_GAZE to AchievementLevel.PRO,
            AchievementType.BLINK_EXPERT to AchievementLevel.PRO,
            AchievementType.FAR_SIGHTED_PRO to AchievementLevel.PRO,
            AchievementType.EAGLE_EYE to AchievementLevel.PRO,
            AchievementType.PALMING_MASTER to AchievementLevel.PRO,
            AchievementType.THE_ALL_SEEING to AchievementLevel.PRO,
            AchievementType.TWO_HUNDRED_EXERCISES to AchievementLevel.PRO,
            AchievementType.FALCON_EYE to AchievementLevel.EXPERT,
            AchievementType.ETERNAL_GUARDIAN to AchievementLevel.EXPERT,
            AchievementType.BLINK_LEGEND to AchievementLevel.EXPERT,
            AchievementType.FAR_SIGHTED_GURU to AchievementLevel.EXPERT,
            AchievementType.PALMING_YOGI to AchievementLevel.EXPERT,
            AchievementType.THOUSAND_AND_ONE to AchievementLevel.EXPERT,
            AchievementType.ENCYCLOPEDIA_OF_SIGHT to AchievementLevel.EXPERT,
            AchievementType.MASTER_OF_CLARITY to AchievementLevel.EXPERT,
            AchievementType.EARLY_BIRD to AchievementLevel.HIDDEN,
            AchievementType.NIGHT_OWL to AchievementLevel.HIDDEN,
            AchievementType.MULTITASKER to AchievementLevel.HIDDEN,
            AchievementType.YIN_YANG to AchievementLevel.HIDDEN,
            AchievementType.TIMELESS_GAZE to AchievementLevel.HIDDEN,
            AchievementType.THINK_TANK to AchievementLevel.HIDDEN,
        )

        AchievementType.entries.forEach { entry: AchievementType ->
            // when
            val level = entry.getLevel()
            // then
            assertThat(level).isEqualTo(levelsMap[entry])
        }
    }

    @Test
    fun `hasNConsecutiveDays should check for consecutive days`() = runTest {
        // given
        val now = Clock.System.now()
        val amountOfDays = 5
        val calendarWithConsecutiveDays = FakeData.getCalendarWithFullDays(now, amountOfDays + 2).toMutableList()
        val calendarWithNoConsecutiveDays = FakeData.getCalendarWithFullDays(now, amountOfDays).toMutableList()
        calendarWithConsecutiveDays.removeAt(1)
        calendarWithNoConsecutiveDays.removeAt(2)
        // when
        val result1 = calendarWithConsecutiveDays.hasNConsecutiveDays(amountOfDays)
        val result2 = calendarWithNoConsecutiveDays.hasNConsecutiveDays(amountOfDays)
        // then
        assertThat(result1).isTrue()
        assertThat(result2).isFalse()
    }
}
