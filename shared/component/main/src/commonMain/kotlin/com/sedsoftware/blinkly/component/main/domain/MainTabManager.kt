package com.sedsoftware.blinkly.component.main.domain

import com.sedsoftware.blinkly.component.main.domain.model.GreetingPeriod
import com.sedsoftware.blinkly.component.main.domain.model.MainCtaState
import com.sedsoftware.blinkly.component.main.domain.model.MainTabData
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyHighlightsProvider
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.extension.asLocalDate
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.ceil
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class MainTabManager(
    private val calendarWatcher: BlinklyCalendarWatcher,
    private val treeProgressWatcher: BlinklyTreeProgressWatcher,
    private val highlightsProvider: BlinklyHighlightsProvider,
    private val settings: BlinklySettings,
    private val timeUtils: BlinklyTimeUtils,
) {

    val calendar: Flow<List<Workout>>
        get() = calendarWatcher.calendar

    val tree: Flow<Tree>
        get() = treeProgressWatcher.tree

    suspend fun getHighlight(): HighlightOfTheDay =
        highlightsProvider.get()

    fun calculateData(calendar: List<Workout>): MainTabData {
        val now = timeUtils.now()
        val timeZone = timeUtils.timeZone()
        val today = now.asLocalDate(timeZone)
        val localTime = now.toLocalDateTime(timeZone).time
        val exercisesToday = calendar.flatMap { it.exercises }
            .filter { it.completedAt.asLocalDate(timeZone) == today }
        val twentyX3Count = exercisesToday.count { it.type == ExerciseType.TWENTY_X3 }
        val palmingCount = exercisesToday.count { it.type == ExerciseType.PALMING }

        return MainTabData(
            greetingPeriod = localTime.toGreetingPeriod(),
            restMinutesToday = exercisesToday.sumRestMinutes(),
            exercisesToday = exercisesToday.size,
            twentyX3Today = twentyX3Count,
            palmingToday = palmingCount,
            dailyProgressPercent = exercisesToday.calculateDailyProgressPercent(),
            treeGrowthStreakDays = calendar.calculateGrowthStreakDays(today),
            ctaState = exercisesToday.calculateCtaState(localTime),
        )
    }

    private fun LocalTime.toGreetingPeriod(): GreetingPeriod =
        when (hour) {
            in MORNING_HOUR_START..MORNING_HOUR_END -> GreetingPeriod.MORNING
            in DAY_HOUR_START..DAY_HOUR_END -> GreetingPeriod.DAY
            else -> GreetingPeriod.EVENING
        }

    private fun List<Exercise>.calculateCtaState(time: LocalTime): MainCtaState {
        val blockACompleted = isBlockACompleted()
        val blockBCompleted = isBlockBCompleted()
        val twentyX3Count = count { it.type == ExerciseType.TWENTY_X3 }
        val hasActivity = isNotEmpty()
        val lastTwentyX3 = filter { it.type == ExerciseType.TWENTY_X3 }.maxByOrNull { it.completedAt }
        val now = timeUtils.now()

        return when {
            blockACompleted && blockBCompleted && twentyX3Count >= MIN_TWENTY_X3_DAILY -> MainCtaState.PerfectDay
            time >= LATE_EVENING_START -> MainCtaState.DayClosing
            time.hour in MORNING_CTA_HOUR_START..MORNING_CTA_HOUR_END && !hasActivity -> MainCtaState.MorningWarmUp
            time >= AFTERNOON_START && !blockACompleted && twentyX3Count > 0 -> MainCtaState.AfternoonWarmUp
            time in EVENING_START..<LATE_EVENING_START && !blockBCompleted && hasActivity -> MainCtaState.EveningRelax
            time.hour in WORK_HOUR_START..WORK_HOUR_END && twentyX3Count < MIN_TWENTY_X3_DAILY &&
                (lastTwentyX3 == null || now - lastTwentyX3.completedAt >= WORK_BREAK_THRESHOLD) -> MainCtaState.WorkBreakDue
            else -> MainCtaState.Idle
        }
    }

    private fun List<Exercise>.calculateDailyProgressPercent(): Int {
        val completedGoals = listOf(
            if (isBlockACompleted()) 1 else 0,
            if (isBlockBCompleted()) 1 else 0,
            count { it.type == ExerciseType.TWENTY_X3 }.coerceAtMost(MIN_TWENTY_X3_DAILY),
        ).sum()

        return (completedGoals * HUNDRED_PERCENT / DAILY_GOALS).coerceIn(0, HUNDRED_PERCENT)
    }

    private fun List<Exercise>.isBlockACompleted(): Boolean =
        map { it.type }.containsAll(BLOCK_A_TYPES)

    private fun List<Exercise>.isBlockBCompleted(): Boolean =
        map { it.type }.containsAll(BLOCK_B_TYPES)

    private fun List<Exercise>.sumRestMinutes(): Int {
        val totalSeconds = sumOf { it.type.durationSeconds() }
        return if (totalSeconds == 0) {
            0
        } else {
            ceil(totalSeconds.toDouble() / SECONDS_IN_MINUTE).toInt()
        }
    }

    private fun ExerciseType.durationSeconds(): Int =
        when (this) {
            ExerciseType.BLINK_BREAK -> ceil(settings.blinkBreakCount * BLINK_BREAK_STEP_SECONDS).toInt()
            ExerciseType.NEAR_FAR_FOCUS -> ceil(
                (settings.nearFarFocusCount * settings.nearFarFocusDuration * NEAR_FAR_PHASES).toDouble()
            ).toInt()
            ExerciseType.DIAGONAL_GAZES -> settings.diagonalGazesCount * DIAGONAL_GAZES_SECONDS_PER_REPEAT
            ExerciseType.FIGURE_EIGHT -> settings.figureEightCount * FIGURE_EIGHT_SECONDS_PER_REPEAT
            ExerciseType.CLOCK_ROLLS -> settings.clockRollsEachSide * CLOCK_ROLLS_SECONDS_PER_SIDE
            ExerciseType.PALMING -> settings.palmingDuration
            ExerciseType.TWENTY_X3 -> TWENTY_X3_SECONDS
        }

    private fun List<Workout>.calculateGrowthStreakDays(today: LocalDate): Int {
        val dates = flatMap { it.exercises }
            .map { it.completedAt.asLocalDate(timeUtils.timeZone()) }
            .toSet()

        if (dates.isEmpty()) return 0

        var currentDate = if (today in dates) today else today.minus(1, DateTimeUnit.DAY)
        var streak = 0

        while (currentDate in dates) {
            streak++
            currentDate = currentDate.minus(1, DateTimeUnit.DAY)
        }

        return streak
    }

    private companion object {
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
        val AFTERNOON_START: LocalTime = LocalTime(hour = 13, minute = 0)
        val EVENING_START: LocalTime = LocalTime(hour = 18, minute = 0)
        val LATE_EVENING_START: LocalTime = LocalTime(hour = 22, minute = 30)
        val WORK_BREAK_THRESHOLD = 20.minutes
        const val MORNING_HOUR_START = 6
        const val MORNING_HOUR_END = 11
        const val DAY_HOUR_START = 12
        const val DAY_HOUR_END = 17
        const val MORNING_CTA_HOUR_START = 6
        const val MORNING_CTA_HOUR_END = 10
        const val WORK_HOUR_START = 11
        const val WORK_HOUR_END = 17
        const val MIN_TWENTY_X3_DAILY = 2
        const val DAILY_GOALS = 4
        const val HUNDRED_PERCENT = 100
        const val SECONDS_IN_MINUTE = 60
        const val TWENTY_X3_SECONDS = 20
        const val BLINK_BREAK_STEP_SECONDS = 0.25
        const val NEAR_FAR_PHASES = 2
        const val DIAGONAL_GAZES_SECONDS_PER_REPEAT = 4
        const val FIGURE_EIGHT_SECONDS_PER_REPEAT = 8
        const val CLOCK_ROLLS_SECONDS_PER_SIDE = 4
    }
}
