package com.sedsoftware.blinkly.component.achievements.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent.Model
import com.sedsoftware.blinkly.component.achievements.store.AchievementsStore
import com.sedsoftware.blinkly.component.achievements.store.AchievementsStoreProvider
import com.sedsoftware.blinkly.domain.BlinklyAchievementsWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AchievementsComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val achievementsWatcher: BlinklyAchievementsWatcher,
    private val achievementsOutput: (ComponentOutput) -> Unit,
) : AchievementsComponent, ComponentContext by componentContext {

    private val store: AchievementsStore =
        instanceKeeper.getStore {
            AchievementsStoreProvider(
                storeFactory = storeFactory,
                achievementsWatcher = achievementsWatcher,
                mainContext = dispatchers.main,
            ).create(autoInit = false)
        }

    init {
        val scope = CoroutineScope(dispatchers.main)

        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            store.labels.collect { label ->
                when (label) {
                    is AchievementsStore.Label.ErrorCaught ->
                        achievementsOutput(ComponentOutput.Common.ErrorCaught(label.exception))
                }
            }
        }

        store.init()

        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onBackClick() {
        achievementsOutput(ComponentOutput.Common.BackPressed)
    }

    override fun onAchievementClick(type: AchievementType) {
        store.accept(AchievementsStore.Intent.AchievementClicked(type))
    }

    override fun onDetailsDismiss() {
        store.accept(AchievementsStore.Intent.DetailsDismissed)
    }
}
