package com.sedsoftware.blinkly.domain.internal

import com.sedsoftware.blinkly.domain.AchievementWatcher
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.achievement.logic.BlinkExpert
import com.sedsoftware.blinkly.domain.achievement.logic.BlinkLegend
import com.sedsoftware.blinkly.domain.achievement.logic.BlinkMaster
import com.sedsoftware.blinkly.domain.achievement.logic.BlinkStarter
import com.sedsoftware.blinkly.domain.achievement.logic.Clockmaker
import com.sedsoftware.blinkly.domain.achievement.logic.Clockwise
import com.sedsoftware.blinkly.domain.achievement.logic.CloseUp
import com.sedsoftware.blinkly.domain.achievement.logic.DiamondEyes
import com.sedsoftware.blinkly.domain.achievement.logic.EagleEye
import com.sedsoftware.blinkly.domain.achievement.logic.EncyclopediaOfSight
import com.sedsoftware.blinkly.domain.achievement.logic.EternalGuardian
import com.sedsoftware.blinkly.domain.achievement.logic.EveningNewbie
import com.sedsoftware.blinkly.domain.achievement.logic.EveningRitual
import com.sedsoftware.blinkly.domain.achievement.logic.ExpressMaster
import com.sedsoftware.blinkly.domain.achievement.logic.FalconEye
import com.sedsoftware.blinkly.domain.achievement.logic.FarSighted
import com.sedsoftware.blinkly.domain.achievement.logic.FarSightedGuru
import com.sedsoftware.blinkly.domain.achievement.logic.FarSightedPro
import com.sedsoftware.blinkly.domain.achievement.logic.FirstSpark
import com.sedsoftware.blinkly.domain.achievement.logic.HundredExercises
import com.sedsoftware.blinkly.domain.achievement.logic.IronGaze
import com.sedsoftware.blinkly.domain.achievement.logic.MasterOfClarity
import com.sedsoftware.blinkly.domain.achievement.logic.Multitasker
import com.sedsoftware.blinkly.domain.achievement.logic.PalmingMaster
import com.sedsoftware.blinkly.domain.achievement.logic.PalmingYogi
import com.sedsoftware.blinkly.domain.achievement.logic.RegularWarmUp
import com.sedsoftware.blinkly.domain.achievement.logic.RelaxEnthusiast
import com.sedsoftware.blinkly.domain.achievement.logic.ReverseMyope
import com.sedsoftware.blinkly.domain.achievement.logic.TheAllSeeing
import com.sedsoftware.blinkly.domain.achievement.logic.TheArtist
import com.sedsoftware.blinkly.domain.achievement.logic.ThirtyExercises
import com.sedsoftware.blinkly.domain.achievement.logic.ThousandAndOne
import com.sedsoftware.blinkly.domain.achievement.logic.ThreeDayStreak
import com.sedsoftware.blinkly.domain.achievement.logic.TwentyX3Rookie
import com.sedsoftware.blinkly.domain.achievement.logic.TwoHundredExercises
import com.sedsoftware.blinkly.domain.extension.getLevel
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class AchievementWatcherImpl(
    private val database: BlinklyDatabase,
    private val notifier: BlinklyNotifier,
    private val settings: BlinklySettings,
    private val timeUtils: BlinklyTimeUtils,
    dispatchers: BlinklyDispatchers,
) : AchievementWatcher {

    private val instances: List<UnlockableAchievement> = registerAchievements()
    private val scope: CoroutineScope = CoroutineScope(dispatchers.io)

    private var observeAchievementsJob: Job? = null
    private var observeCalendarJob: Job? = null

    private var latestAchievements: List<Achievement> = emptyList()
    private var latestCalendar: List<Workout> = emptyList()

    override fun start() {
        observeAchievementsJob?.cancel()
        observeAchievementsJob = scope.launch {
            database.currentAchievements().collect { achievements: List<Achievement> ->
                latestAchievements = achievements
                checkIfUnlocked()
            }
        }

        observeCalendarJob?.cancel()
        observeCalendarJob = scope.launch {
            database.currentCalendar().collect { calendar: List<Workout> ->
                latestCalendar = calendar
                checkIfUnlocked()
            }
        }
    }

    override fun stop() {
        observeCalendarJob?.cancel()
        observeAchievementsJob?.cancel()
        scope.cancel()
    }

    private suspend fun checkIfUnlocked() {
        instances.forEach { instance: UnlockableAchievement ->
            if (instance.unlocked(latestAchievements, latestCalendar) && !isAchievementUnlocked(instance.type)) {
                saveUnlockedAchievement(instance.type)
                notifyAboutUnlockedAchievement(instance.type)
            }
        }
    }

    private suspend fun saveUnlockedAchievement(achievementType: AchievementType) {
        val achievement = Achievement(
            type = achievementType,
            level = achievementType.getLevel(),
            unlockedAt = timeUtils.now(),
        )
        database.unlockAchievement(achievement)
    }

    private fun notifyAboutUnlockedAchievement(achievementType: AchievementType) {
        notifier.achievementUnlocked(achievementType)
    }

    private fun isAchievementUnlocked(achievementType: AchievementType) : Boolean {
        val types = latestAchievements.map { it.type }
        return types.contains(achievementType)
    }

    private fun registerAchievements(): List<UnlockableAchievement> = listOf(
        FirstSpark(),
        BlinkStarter(settings.blinkBreakCount),
        CloseUp(),
        Clockwise(),
        EveningNewbie(settings.palmingDuration),
        TwentyX3Rookie(),
        ThreeDayStreak(),
        DiamondEyes(),
        BlinkMaster(settings.blinkBreakCount),
        FarSighted(),
        TheArtist(settings.figureEightCount),
        Clockmaker(),
        ReverseMyope(),
        RelaxEnthusiast(settings.palmingDuration),
        RegularWarmUp(),
        EveningRitual(),
        ExpressMaster(),
        ThirtyExercises(),
        HundredExercises(),
        IronGaze(),
        BlinkExpert(settings.blinkBreakCount),
        FarSightedPro(),
        EagleEye(),
        PalmingMaster(settings.palmingDuration),
        TheAllSeeing(),
        TwoHundredExercises(),
        FalconEye(),
        EternalGuardian(),
        BlinkLegend(settings.blinkBreakCount),
        FarSightedGuru(),
        PalmingYogi(settings.palmingDuration),
        ThousandAndOne(),
        EncyclopediaOfSight(),
        MasterOfClarity(),
    )
}
