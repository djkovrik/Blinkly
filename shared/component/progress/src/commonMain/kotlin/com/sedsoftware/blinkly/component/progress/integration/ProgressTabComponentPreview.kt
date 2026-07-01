@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.component.progress.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent.CalendarDay
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent.CalendarDayState
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent.Model
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Instant

class ProgressTabComponentPreview(
    private val calendarWeeks: List<List<CalendarDay?>> = previewCalendarWeeks(),
    private val tree: Tree? = Tree(TreeStage.MAGNIFICENT, TreeType.QUERCUS_ROBUR, FULL_PROGRESS),
    private val recentAchievements: List<Achievement?> = previewAchievements(),
) : ProgressTabComponent {

    override val model: Value<Model> = MutableValue(
        Model(
            calendarWeeks = calendarWeeks,
            tree = tree,
            recentAchievements = recentAchievements,
        )
    )

    override fun onAchievementsClick() = Unit
    override fun onGardenClick() = Unit
}

private fun previewCalendarWeeks(): List<List<CalendarDay?>> {
    val firstDay = LocalDate(year = 2026, month = 3, day = 1)
    val completedDays = setOf(2, 5, 6, 7, 9, 18, 19, 27)
    val perfectDays = setOf(6, 7, 18)
    val days = buildList<CalendarDay?> {
        repeat(firstDay.dayOfWeek.ordinal) {
            add(null)
        }

        repeat(times = 31) { index ->
            val day = index + 1
            val date = firstDay.plus(index, DateTimeUnit.DAY)
            val state = when (day) {
                in perfectDays -> CalendarDayState.PERFECT
                in completedDays -> CalendarDayState.WORKOUT
                else -> CalendarDayState.EMPTY
            }

            add(CalendarDay(date = date, state = state))
        }

        while (size % DAYS_IN_WEEK != 0) {
            add(null)
        }
    }

    return days.chunked(size = DAYS_IN_WEEK)
}

private fun previewAchievements(): List<Achievement?> =
    listOf(
        Achievement(
            type = AchievementType.DIAMOND_EYES,
            level = AchievementLevel.INTERMEDIATE,
            unlockedAt = Instant.parse("2026-03-18T18:30:00Z"),
        ),
        Achievement(
            type = AchievementType.BLINK_MASTER,
            level = AchievementLevel.INTERMEDIATE,
            unlockedAt = Instant.parse("2026-03-16T12:00:00Z"),
        ),
        Achievement(
            type = AchievementType.FAR_SIGHTED,
            level = AchievementLevel.INTERMEDIATE,
            unlockedAt = Instant.parse("2026-03-15T09:00:00Z"),
        ),
    )

private const val DAYS_IN_WEEK = 7
private const val FULL_PROGRESS = 100f
