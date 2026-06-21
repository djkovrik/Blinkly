package com.sedsoftware.blinkly.component.newreminder.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.Model
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.ScheduleType
import com.sedsoftware.blinkly.component.newreminder.domain.AddNewReminderManager
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStoreProvider
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.utils.asValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

class AddNewReminderComponentDefault(
    private val componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val dispatchers: BlinklyDispatchers,
    private val reminderManager: BlinklyReminderManager,
    private val addNewReminderOutput: (ComponentOutput) -> Unit,
) : AddNewReminderComponent, ComponentContext by componentContext {

    private val store: AddNewReminderStore =
        instanceKeeper.getStore {
            AddNewReminderStoreProvider(
                storeFactory = storeFactory,
                manager = AddNewReminderManager(reminderManager),
                mainContext = dispatchers.main,
                ioContext = dispatchers.io,
            ).create()
        }

    init {
        val scope = CoroutineScope(dispatchers.main)

        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is AddNewReminderStore.Label.ReminderCreated -> addNewReminderOutput(ComponentOutput.Common.BackPressed)
                    is AddNewReminderStore.Label.ErrorCaught -> addNewReminderOutput(ComponentOutput.Common.ErrorCaught(label.exception))
                }
            }
        }

        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override val model: Value<Model> = store.asValue().map(stateToModel)

    override fun onBackClick() {
        addNewReminderOutput(ComponentOutput.Common.BackPressed)
    }

    override fun onScheduleTypeSelect(type: ScheduleType) {
        store.accept(AddNewReminderStore.Intent.ScheduleTypeSelected(type.toStoreType()))
    }

    override fun onDailyTimeSelect(time: LocalTime) {
        store.accept(AddNewReminderStore.Intent.DailyTimeSelected(time))
    }

    override fun onWeeklyTimeSelect(time: LocalTime) {
        store.accept(AddNewReminderStore.Intent.WeeklyTimeSelected(time))
    }

    override fun onWeeklyDaySelect(dayOfWeek: DayOfWeek) {
        store.accept(AddNewReminderStore.Intent.WeeklyDaySelected(dayOfWeek))
    }

    override fun onPeriodTimeFromSelect(time: LocalTime) {
        store.accept(AddNewReminderStore.Intent.PeriodTimeFromSelected(time))
    }

    override fun onPeriodTimeUntilSelect(time: LocalTime) {
        store.accept(AddNewReminderStore.Intent.PeriodTimeUntilSelected(time))
    }

    override fun onPeriodIntervalSelect(interval: Int) {
        store.accept(AddNewReminderStore.Intent.PeriodIntervalSelected(interval))
    }

    override fun onPeriodDayToggle(dayOfWeek: DayOfWeek) {
        store.accept(AddNewReminderStore.Intent.PeriodDayToggled(dayOfWeek))
    }

    override fun onCreateClick() {
        store.accept(AddNewReminderStore.Intent.CreateClicked)
    }

    override fun onValidationMessageShown() {
        store.accept(AddNewReminderStore.Intent.ValidationMessageShown)
    }
}
