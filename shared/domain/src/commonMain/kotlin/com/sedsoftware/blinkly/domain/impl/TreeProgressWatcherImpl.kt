package com.sedsoftware.blinkly.domain.impl

import com.sedsoftware.blinkly.domain.TreeProgressWatcher
import com.sedsoftware.blinkly.domain.extension.asLocalDate
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.time.Instant

class TreeProgressWatcherImpl(
    private val database: BlinklyDatabase,
    private val timeUtils: BlinklyTimeUtils,
    dispatchers: BlinklyDispatchers,
) : TreeProgressWatcher {

    private val scope: CoroutineScope = CoroutineScope(dispatchers.io + SupervisorJob())

    override val tree: Flow<Tree> = flow {
        val calendarFlow = database.currentCalendar()
        emitAll(
            calendarFlow.map { workouts ->
                calculateCurrentTree(workouts, timeUtils.now())
            }
        )
    }.shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT),
        replay = 1,
    )

    private fun calculateCurrentTree(workouts: List<Workout>, now: Instant): Tree {
        if (workouts.isEmpty()) {
            return Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f)
        }

        val today = now.asLocalDate()
        val exercisesByDate = workouts.flatMap { it.exercises }
            .groupBy { it.completedAt.asLocalDate() }

        val dailyProgressByDate = exercisesByDate.mapValues { (_, exercises) ->
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

        val cappedBefore = progressBeforeToday.coerceAtMost(280f)

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

        val finalProgress = (startingProgress + todayProgress).coerceAtMost(currentStage.threshold)
        val percentProgress = (finalProgress / currentStage.threshold * HUNDRED_PERCENTS).coerceIn(0f, HUNDRED_PERCENTS)

        return Tree(currentStage, currentType, percentProgress)
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

    private companion object {
        const val SUBSCRIPTION_STOP_TIMEOUT = 5000L
        const val HUNDRED_PERCENTS = 100f
        const val STEP_HALF = 0.5f
        const val STEP_FULL = 1.0f
    }
}
