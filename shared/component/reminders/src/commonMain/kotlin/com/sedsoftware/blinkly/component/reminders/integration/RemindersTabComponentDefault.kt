package com.sedsoftware.blinkly.component.reminders.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Model
import com.sedsoftware.blinkly.component.reminders.domain.RemindersManager
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore
import com.sedsoftware.blinkly.component.reminders.store.RemindersStoreProvider
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RemindersTabComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val reminderManager: BlinklyReminderManager,
    private val remindersTabOutput: (ComponentOutput) -> Unit,
) : RemindersTabComponent, ComponentContext by componentContext {

    private val store: RemindersStore =
        instanceKeeper.getStore {
            RemindersStoreProvider(
                storeFactory = storeFactory,
                manager = RemindersManager(reminderManager),
                mainContext = dispatchers.main,
                ioContext = dispatchers.io,
            ).create()
        }

    init {
        val scope = CoroutineScope(dispatchers.main)

        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is RemindersStore.Label.ErrorCaught -> remindersTabOutput(ComponentOutput.Common.ErrorCaught(label.exception))
                }
            }
        }

        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onAddNewClick() {
        remindersTabOutput(ComponentOutput.Reminders.OpenAddNew)
    }

    override fun onDeleteReminder(uuid: String) {
        store.accept(RemindersStore.Intent.DeleteReminder(uuid))
    }

    override fun onUndoDelete() {
        store.accept(RemindersStore.Intent.UndoDelete)
    }

    override fun onDeletedMessageShown() {
        store.accept(RemindersStore.Intent.DeleteMessageShown)
    }
}
