package com.sedsoftware.blinkly.domain.achievement.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.base.BaseAchievementTest
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.test.Test

internal class EarlyBirdTest : BaseAchievementTest() {

    override val achievement: UnlockableAchievement = EarlyBird(timeZone)

    @Test
    fun `when calendar and achievements match logic then unlocked`() = runTest {
        // given
        val calendar = calendarAtHours(7, 12)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isTrue()
    }

    @Test
    fun `when calendar and achievements do not match logic then not unlocked`() = runTest {
        // given
        val calendar = calendarAtHours(8, 12)
        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)
        // then
        assertThat(unlocked).isFalse()
    }

    @Test
    fun `when exercise is completed before 5 AM then unlocked`() = runTest {
        // given
        val calendar = calendarAtHours(4)

        // when
        val unlocked = achievement.unlocked(emptyAchievements, calendar)

        // then
        assertThat(unlocked).isTrue()
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
