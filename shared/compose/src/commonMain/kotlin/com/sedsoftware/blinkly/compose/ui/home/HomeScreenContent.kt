package com.sedsoftware.blinkly.compose.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.component.home.model.HomeScreenTab
import com.sedsoftware.blinkly.compose.ui.home.bottomnavigation.BottomNavigationBar
import com.sedsoftware.blinkly.compose.ui.home.tabs.MainTabContent
import com.sedsoftware.blinkly.compose.ui.home.tabs.ProgressTabContent
import com.sedsoftware.blinkly.compose.ui.home.tabs.RemindersTabContent
import com.sedsoftware.blinkly.compose.ui.home.tabs.TrainingsTabContent

@Composable
internal fun HomeScreenContent(
    component: HomeScreenComponent,
    modifier: Modifier = Modifier,
) {
    val stack by component.childStack.subscribeAsState()
    val activeComponent: HomeScreenComponent.Child = stack.active.instance
    val activeTab: HomeScreenTab = when (activeComponent) {
        is HomeScreenComponent.Child.MainTab -> HomeScreenTab.MAIN
        is HomeScreenComponent.Child.TrainingsTab -> HomeScreenTab.TRAININGS
        is HomeScreenComponent.Child.ProgressTab -> HomeScreenTab.PROGRESS
        is HomeScreenComponent.Child.RemindersTab -> HomeScreenTab.REMINDERS
    }

    Column(modifier = modifier) {
        ChildrenContent(
            component = component,
            modifier = Modifier.weight(weight = 1f),
        )

        BottomNavigationBar(
            activeTab = activeTab,
            onTabClick = component::onTabClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ChildrenContent(
    component: HomeScreenComponent,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(animator = fade()),
        modifier = modifier,
    ) {
        when (val child = it.instance) {
            is HomeScreenComponent.Child.MainTab -> MainTabContent(child.component)
            is HomeScreenComponent.Child.TrainingsTab -> TrainingsTabContent(child.component)
            is HomeScreenComponent.Child.ProgressTab -> ProgressTabContent(child.component)
            is HomeScreenComponent.Child.RemindersTab -> RemindersTabContent(child.component)
        }
    }
}
