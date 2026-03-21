package com.sedsoftware.blinkly.component.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent
import com.sedsoftware.blinkly.component.blocka.BlockAComponent
import com.sedsoftware.blinkly.component.blockb.BlockBComponent
import com.sedsoftware.blinkly.component.blockc.BlockCComponent
import com.sedsoftware.blinkly.component.garden.GardenComponent
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent
import com.sedsoftware.blinkly.component.preferences.PreferencesComponent

interface RootComponent : BackHandlerOwner {

    val childStack: Value<ChildStack<*, Child>>

    fun onBack()

    sealed class Child {
        data class Onboarding(val component: OnboardingComponent) : Child()
        data class HomeScreen(val component: HomeScreenComponent) : Child()
        data class Preferences(val component: PreferencesComponent) : Child()
        data class BlockA(val component: BlockAComponent) : Child()
        data class BlockB(val component: BlockBComponent) : Child()
        data class BlockC(val component: BlockCComponent) : Child()
        data class Achievements(val component: AchievementsComponent) : Child()
        data class Garden(val component: GardenComponent) : Child()
        data class AddNewReminder(val component: AddNewReminderComponent) : Child()
    }
}
