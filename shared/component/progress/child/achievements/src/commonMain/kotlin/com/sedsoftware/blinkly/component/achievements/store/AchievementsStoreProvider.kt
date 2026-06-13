@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.component.achievements.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.achievements.store.AchievementsStore.Intent
import com.sedsoftware.blinkly.component.achievements.store.AchievementsStore.Label
import com.sedsoftware.blinkly.component.achievements.store.AchievementsStore.State
import com.sedsoftware.blinkly.domain.BlinklyAchievementsWatcher
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.utils.StoreProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class AchievementsStoreProvider(
    private val storeFactory: StoreFactory,
    private val achievementsWatcher: BlinklyAchievementsWatcher,
    private val mainContext: CoroutineContext,
) {
    @StoreProvider
    fun create(autoInit: Boolean = true): AchievementsStore =
        object : AchievementsStore, Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
            name = "AchievementsStore",
            initialState = State(),
            autoInit = autoInit,
            bootstrapper = coroutineBootstrapper(mainContext) {
                dispatch(Action.ObserveAchievements)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                onAction<Action.ObserveAchievements> {
                    launch {
                        achievementsWatcher.achievements
                            .catch { publish(Label.ErrorCaught(it)) }
                            .collect { achievements ->
                                dispatch(Msg.AchievementsUpdated(achievements))
                            }
                    }
                }

                onIntent<Intent.AchievementClicked> {
                    dispatch(Msg.AchievementSelected(it.type))
                }

                onIntent<Intent.DetailsDismissed> {
                    dispatch(Msg.DetailsDismissed)
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.AchievementsUpdated -> {
                        val sorted = msg.achievements.sortedWith(compareBy({ it.level.index }, { it.type.index }))
                        copy(
                            achievements = sorted,
                            selectedType = selectedType?.takeIf { type ->
                                sorted.any { achievement -> achievement.type == type && achievement.unlockedAt != null }
                            },
                        )
                    }

                    is Msg.AchievementSelected -> {
                        val selected = achievements.firstOrNull { it.type == msg.type }
                        if (selected?.unlockedAt != null) {
                            copy(selectedType = msg.type)
                        } else {
                            this
                        }
                    }

                    is Msg.DetailsDismissed -> copy(
                        selectedType = null,
                    )
                }
            }
        ) {}

    sealed interface Action {
        data object ObserveAchievements : Action
    }

    sealed interface Msg {
        data class AchievementsUpdated(val achievements: List<Achievement>) : Msg
        data class AchievementSelected(val type: AchievementType) : Msg
        data object DetailsDismissed : Msg
    }
}
