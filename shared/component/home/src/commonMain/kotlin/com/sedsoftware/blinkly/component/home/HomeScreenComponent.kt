package com.sedsoftware.blinkly.component.home

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.home.model.HomeScreenTab
import com.sedsoftware.blinkly.component.main.MainTabComponent
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent
import com.sedsoftware.blinkly.component.trainings.TrainingsTabComponent

interface HomeScreenComponent {

    val childStack: Value<ChildStack<*, Child>>

    fun onTabClick(tab: HomeScreenTab)

    sealed class Child {
        class MainTab(val component: MainTabComponent) : Child()
        class TrainingsTab(val component: TrainingsTabComponent) : Child()
        class ProgressTab(val component: ProgressTabComponent) : Child()
        class RemindersTab(val component: RemindersTabComponent) : Child()
    }
}
