package com.sedsoftware.blinkly.domain.impl

import com.sedsoftware.blinkly.domain.AchievementsWatcher
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
import com.sedsoftware.blinkly.domain.achievement.logic.EarlyBird
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
import com.sedsoftware.blinkly.domain.achievement.logic.NightOwl
import com.sedsoftware.blinkly.domain.achievement.logic.PalmingMaster
import com.sedsoftware.blinkly.domain.achievement.logic.PalmingYogi
import com.sedsoftware.blinkly.domain.achievement.logic.RegularWarmUp
import com.sedsoftware.blinkly.domain.achievement.logic.RelaxEnthusiast
import com.sedsoftware.blinkly.domain.achievement.logic.ReverseMyope
import com.sedsoftware.blinkly.domain.achievement.logic.TheAllSeeing
import com.sedsoftware.blinkly.domain.achievement.logic.TheArtist
import com.sedsoftware.blinkly.domain.achievement.logic.ThinkTank
import com.sedsoftware.blinkly.domain.achievement.logic.ThirtyExercises
import com.sedsoftware.blinkly.domain.achievement.logic.ThousandAndOne
import com.sedsoftware.blinkly.domain.achievement.logic.ThreeDayStreak
import com.sedsoftware.blinkly.domain.achievement.logic.TimelessGaze
import com.sedsoftware.blinkly.domain.achievement.logic.TwentyX3Rookie
import com.sedsoftware.blinkly.domain.achievement.logic.TwoHundredExercises
import com.sedsoftware.blinkly.domain.achievement.logic.YinYang
import com.sedsoftware.blinkly.domain.extension.getLevel
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

internal class AchievementsWatcherImpl(
    private val database: BlinklyDatabase,
    private val notifier: BlinklyNotifier,
    private val settings: BlinklySettings,
    private val timeUtils: BlinklyTimeUtils,
    dispatchers: BlinklyDispatchers,
) : AchievementsWatcher {

    private val instances: List<UnlockableAchievement> = registerAchievements()
    private val scope: CoroutineScope = CoroutineScope(dispatchers.io + SupervisorJob())
    private val _currentAchievements = MutableStateFlow<List<Achievement>>(emptyList())

    private val lightThemeWorkoutIndex: () -> Int
        get() = { settings.lightThemeWorkoutIndex }

    private val darkThemeWorkoutIndex: () -> Int
        get() = { settings.darkThemeWorkoutIndex }

    private val achievementsFlow: Flow<List<Achievement>> = flow {
        val achievementsSource: Flow<List<Achievement>> = database.currentAchievements()
        val calendarSource: Flow<List<Workout>> = database.currentCalendar()
        emitAll(
            combine(achievementsSource, calendarSource) { unlockedAchievements, currentCalendar ->
                if (currentCalendar.isNotEmpty()) {
                    refreshYinYangState(currentCalendar)
                }
                checkIfUnlocked(unlockedAchievements, currentCalendar)
                buildFullAchievementsList(unlockedAchievements)
            }
        )
    }
        .flowOn(dispatchers.io)
        .catch { /* handle error if needed */ }
        .onEach { _currentAchievements.value = it }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = SUBSCRIPTION_STOP_TIMEOUT),
            replay = 1,
        )

    override val achievements: Flow<List<Achievement>>
        get() = achievementsFlow

    private suspend fun checkIfUnlocked(
        achievements: List<Achievement>,
        calendar: List<Workout>,
    ) {
        instances.forEach { instance ->
            if (instance.unlocked(achievements, calendar) && !isAchievementUnlocked(instance.type)) {
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

    private fun refreshYinYangState(calendar: List<Workout>) {
        if (settings.lightThemeWorkoutIndex == 0 && settings.themeState == ThemeState.LIGHT) {
            settings.lightThemeWorkoutIndex = calendar.size
        }

        if (settings.darkThemeWorkoutIndex == 0 && settings.themeState == ThemeState.DARK) {
            settings.darkThemeWorkoutIndex = calendar.size
        }
    }

    private suspend fun notifyAboutUnlockedAchievement(achievementType: AchievementType) {
        notifier.achievementUnlocked(achievementType)
    }

    private fun isAchievementUnlocked(achievementType: AchievementType): Boolean {
        return _currentAchievements.value.any { it.type == achievementType }
    }

    private fun buildFullAchievementsList(unlockedAchievements: List<Achievement>): List<Achievement> {
        val unlocked: Map<AchievementType, Achievement> = unlockedAchievements.associateBy { it.type }
        return AchievementType.entries
            .map { type -> unlocked[type] ?: Achievement(type, type.getLevel(), null) }
    }

    private fun registerAchievements(): List<UnlockableAchievement> =
        listOf(
            FirstSpark(),
            BlinkStarter(settings.blinkBreakCount),
            CloseUp(),
            Clockwise(),
            EveningNewbie(settings.palmingDuration),
            TwentyX3Rookie(),
            ThreeDayStreak(timeUtils.timeZone()),
            DiamondEyes(timeUtils.timeZone()),
            BlinkMaster(settings.blinkBreakCount),
            ExpressMaster(),
            TheArtist(settings.figureEightCount),
            Clockmaker(),
            ReverseMyope(),
            RelaxEnthusiast(settings.palmingDuration),
            RegularWarmUp(),
            EveningRitual(),
            FarSighted(),
            ThirtyExercises(),
            HundredExercises(),
            IronGaze(timeUtils.timeZone()),
            BlinkExpert(settings.blinkBreakCount),
            FarSightedPro(),
            EagleEye(),
            PalmingMaster(settings.palmingDuration),
            TheAllSeeing(),
            TwoHundredExercises(),
            FalconEye(timeUtils.timeZone()),
            EternalGuardian(timeUtils.timeZone()),
            BlinkLegend(settings.blinkBreakCount),
            FarSightedGuru(),
            PalmingYogi(settings.palmingDuration),
            ThousandAndOne(),
            EncyclopediaOfSight(),
            MasterOfClarity(),
            EarlyBird(timeUtils.timeZone()),
            NightOwl(timeUtils.timeZone()),
            Multitasker(),
            YinYang(lightThemeWorkoutIndex, darkThemeWorkoutIndex),
            TimelessGaze(timeUtils.timeZone()),
            ThinkTank(),
        )

    private companion object {
        const val SUBSCRIPTION_STOP_TIMEOUT = 5000L
    }
}
