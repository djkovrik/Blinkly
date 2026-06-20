package com.sedsoftware.blinkly.domain.impl

import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.extension.asLocalDate
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeGarden
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.TimeZone
import kotlin.math.ceil

class BlinklyTreeProgressWatcherImpl(
    private val timeUtils: BlinklyTimeUtils,
    database: BlinklyDatabase,
    dispatchers: BlinklyDispatchers,
) : BlinklyTreeProgressWatcher {

    private val scope: CoroutineScope = CoroutineScope(dispatchers.io + SupervisorJob())

    private val calendar: Flow<List<Workout>> = database.currentCalendar()
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT),
            replay = 1,
        )

    override val tree: Flow<Tree> = calendar
        .map(::calculateCurrentTree)
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT),
            replay = 1,
        )

    override val garden: Flow<TreeGarden> = calendar
        .map(::calculateGarden)
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT),
            replay = 1,
        )

    private fun calculateCurrentTree(workouts: List<Workout>): Tree {
        if (workouts.isEmpty()) {
            return Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f)
        }

        val progress = calculateProgress(workouts)
        val cappedBefore = progress.beforeToday.coerceAtMost(MAX_GARDEN_PROGRESS)

        val (baseType, baseStage, accumulatedBefore) = getStageAndProgress(cappedBefore)

        var currentType = baseType
        var currentStage = baseStage
        var startingProgress = accumulatedBefore

        if (accumulatedBefore >= baseStage.threshold) {
            val (nextType, nextStage) = getNextStageAndType(baseType, baseStage)
            if (currentType == baseType && currentStage == baseStage) {
                return Tree(currentStage, currentType, HUNDRED_PERCENTS)
            }
            currentType = nextType
            currentStage = nextStage
            startingProgress = 0f
        }

        val finalProgress = (startingProgress + progress.today).coerceAtMost(currentStage.threshold)
        val percentProgress = (finalProgress / currentStage.threshold * HUNDRED_PERCENTS).coerceIn(0f, HUNDRED_PERCENTS)

        return Tree(currentStage, currentType, percentProgress)
    }

    private fun calculateGarden(workouts: List<Workout>): TreeGarden {
        val progress = calculateProgress(workouts)
        val totalProgress = (progress.beforeToday + progress.today).coerceAtMost(MAX_GARDEN_PROGRESS)
        val grownTreesCount = (totalProgress / TREE_FULL_PROGRESS).toInt().coerceIn(0, TreeType.entries.size)
        val nextTreeType = TreeType.entries.getOrNull(grownTreesCount)
        val daysToNextTree = nextTreeType?.let {
            val nextTreeThreshold = TREE_FULL_PROGRESS * (grownTreesCount + 1)
            ceil(nextTreeThreshold - totalProgress).toInt().coerceAtLeast(0)
        }

        return TreeGarden(
            currentTree = calculateCurrentTree(workouts),
            grownTrees = TreeType.entries
                .take(grownTreesCount)
                .map { type -> Tree(TreeStage.MAGNIFICENT, type, HUNDRED_PERCENTS) },
            totalTrees = TreeType.entries.size,
            nextTreeType = nextTreeType,
            daysToNextTree = daysToNextTree,
        )
    }

    private fun calculateProgress(workouts: List<Workout>): Progress {
        val now = timeUtils.now()
        val timeZone: TimeZone = timeUtils.timeZone()
        val today = now.asLocalDate(timeZone)
        val dailyProgressByDate = workouts.flatMap { it.exercises }
            .groupBy { it.completedAt.asLocalDate(timeZone) }
            .mapValues { (_, exercises) ->
                val uniqueBlocks = exercises.map { it.block }.toSet().size
                if (uniqueBlocks == 1) STEP_HALF else STEP_FULL
            }

        var progressBeforeToday = 0f
        var todayProgress = 0f

        dailyProgressByDate.forEach { (date, progress) ->
            when {
                date < today -> progressBeforeToday += progress
                date == today -> todayProgress = progress
            }
        }

        return Progress(
            beforeToday = progressBeforeToday,
            today = todayProgress,
        )
    }

    private fun getStageAndProgress(total: Float): Triple<TreeType, TreeStage, Float> {
        var remaining = total.coerceAtLeast(0f)

        for (type in TreeType.entries) {
            for (stage in TreeStage.entries) {
                if (remaining < stage.threshold) {
                    return Triple(type, stage, remaining)
                }
                remaining -= stage.threshold
            }
        }

        return Triple(
            TreeType.QUERCUS_ROBUR,
            TreeStage.MAGNIFICENT,
            TreeStage.MAGNIFICENT.threshold,
        )
    }

    private fun getNextStageAndType(type: TreeType, stage: TreeStage): Pair<TreeType, TreeStage> {
        val stageIndex = TreeStage.entries.indexOf(stage)
        if (stageIndex < TreeStage.entries.lastIndex) {
            return type to TreeStage.entries[stageIndex + 1]
        }
        val typeIndex = TreeType.entries.indexOf(type)
        if (typeIndex < TreeType.entries.lastIndex) {
            return TreeType.entries[typeIndex + 1] to TreeStage.TINY
        }
        return type to stage
    }

    private data class Progress(
        val beforeToday: Float,
        val today: Float,
    )

    private companion object {
        const val SUBSCRIPTION_STOP_TIMEOUT = 5000L
        const val HUNDRED_PERCENTS = 100f
        const val MAX_GARDEN_PROGRESS = 280f
        val TREE_FULL_PROGRESS: Float = TreeStage.entries.sumOf { it.threshold.toDouble() }.toFloat()
        const val STEP_HALF = 0.5f
        const val STEP_FULL = 1.0f
    }
}
