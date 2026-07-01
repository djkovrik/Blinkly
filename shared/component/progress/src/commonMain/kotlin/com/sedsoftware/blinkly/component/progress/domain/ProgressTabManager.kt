@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.component.progress.domain

import com.sedsoftware.blinkly.component.progress.domain.model.ProgressCalendarDay
import com.sedsoftware.blinkly.component.progress.domain.model.ProgressCalendarDayState
import com.sedsoftware.blinkly.domain.BlinklyAchievementsWatcher
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.extension.asLocalDate
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

internal class ProgressTabManager(
    private val calendarWatcher: BlinklyCalendarWatcher,
    private val achievementsWatcher: BlinklyAchievementsWatcher,
    private val treeProgressWatcher: BlinklyTreeProgressWatcher,
    private val timeUtils: BlinklyTimeUtils,
) {

    val calendar: Flow<List<Workout>>
        get() = calendarWatcher.calendar

    val achievements: Flow<List<Achievement>>
        get() = achievementsWatcher.achievements

    val tree: Flow<Tree>
        get() = treeProgressWatcher.tree

    fun calculateCalendarWeeks(calendar: List<Workout>): Result<List<List<ProgressCalendarDay?>>> = runCatching {
        val timeZone = timeUtils.timeZone()
        val today = timeUtils.now().toLocalDateTime(timeZone).date
        val firstDayOfMonth = LocalDate(year = today.year, month = today.month, day = 1)
        val lastDayOfMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        val exercisesByDate = calendar.flatMap { it.exercises }.groupBy { it.completedAt.asLocalDate(timeZone) }
        val leadingEmptyDays = firstDayOfMonth.dayOfWeek.ordinal
        val calendarDays = buildList<ProgressCalendarDay?> {
            repeat(leadingEmptyDays) {
                add(null)
            }

            for (day in 1..lastDayOfMonth.day) {
                val date = LocalDate(year = today.year, month = today.month, day = day)
                val exercises = exercisesByDate[date].orEmpty()

                add(
                    ProgressCalendarDay(
                        date = date,
                        state = exercises.toCalendarDayState(),
                    )
                )
            }

            while (size % DAYS_IN_WEEK != 0) {
                add(null)
            }
        }

        calendarDays.chunked(size = DAYS_IN_WEEK)
    }

    fun calculateRecentAchievements(achievements: List<Achievement>): Result<List<Achievement?>> = runCatching {
        val unlocked = achievements
            .filter { it.unlockedAt != null }
            .sortedByDescending { it.unlockedAt }
            .take(RECENT_ACHIEVEMENTS_COUNT)

        buildList {
            addAll(unlocked)
            repeat(RECENT_ACHIEVEMENTS_COUNT - unlocked.size) {
                add(null)
            }
        }
    }

    private fun List<Exercise>.toCalendarDayState(): ProgressCalendarDayState =
        when {
            isEmpty() -> ProgressCalendarDayState.EMPTY
            isPerfectDay() -> ProgressCalendarDayState.PERFECT
            else -> ProgressCalendarDayState.WORKOUT
        }

    private fun List<Exercise>.isPerfectDay(): Boolean =
        isBlockACompleted() &&
            isBlockBCompleted() &&
            count { it.type == ExerciseType.TWENTY_X3 } >= MIN_TWENTY_X3_DAILY

    private fun List<Exercise>.isBlockACompleted(): Boolean =
        map { it.type }.containsAll(BLOCK_A_TYPES)

    private fun List<Exercise>.isBlockBCompleted(): Boolean =
        map { it.type }.containsAll(BLOCK_B_TYPES)

    private companion object {
        const val DAYS_IN_WEEK = 7
        const val RECENT_ACHIEVEMENTS_COUNT = 3
        const val MIN_TWENTY_X3_DAILY = 2

        val BLOCK_A_TYPES: Set<ExerciseType> = setOf(
            ExerciseType.BLINK_BREAK,
            ExerciseType.NEAR_FAR_FOCUS,
            ExerciseType.DIAGONAL_GAZES,
        )
        val BLOCK_B_TYPES: Set<ExerciseType> = setOf(
            ExerciseType.FIGURE_EIGHT,
            ExerciseType.CLOCK_ROLLS,
            ExerciseType.PALMING,
        )
    }
}
