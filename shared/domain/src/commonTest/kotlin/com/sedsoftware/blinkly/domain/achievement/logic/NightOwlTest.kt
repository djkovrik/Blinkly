package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.base.BaseAchievementTest
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.test.Test

internal class NightOwlTest : BaseAchievementTest() {

    override val achievement: UnlockableAchievement = NightOwl(timeZone)

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val calendar = calendarAtHours(12, 23)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val calendar = calendarAtHours(0, 1, 22)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isFalse()
    }

    private fun calendarAtHours(vararg hours: Int): List<Workout> =
        listOf(
            Workout(
                exercises = hours.map { hour ->
                    Exercise(
                        ExerciseBlock.A,
                        ExerciseType.BLINK_BREAK,
                        LocalDateTime(2024, 1, 15, hour, 0).toInstant(timeZone),
                    )
                }
            )
        )
}
