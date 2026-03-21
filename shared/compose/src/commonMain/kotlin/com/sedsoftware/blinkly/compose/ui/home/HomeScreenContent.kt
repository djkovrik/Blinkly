package com.sedsoftware.blinkly.compose.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.tab_main
import blinkly.shared.compose.generated.resources.tab_progress
import blinkly.shared.compose.generated.resources.tab_reminders
import blinkly.shared.compose.generated.resources.tab_trainings
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.component.home.model.HomeScreenTab
import com.sedsoftware.blinkly.compose.ui.home.tabs.MainTabContent
import com.sedsoftware.blinkly.compose.ui.home.tabs.ProgressTabContent
import com.sedsoftware.blinkly.compose.ui.home.tabs.RemindersTabContent
import com.sedsoftware.blinkly.compose.ui.home.tabs.TrainingsTabContent
import org.jetbrains.compose.resources.painterResource

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

@Composable
private fun BottomNavigationBar(
    activeTab: HomeScreenTab,
    onTabClick: (HomeScreenTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier.navigationBarsPadding()) {
        NavigationBarItem(
            label = { Text("Main") },
            icon = {
                Icon(
                    painter = painterResource(resource = Res.drawable.tab_main),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(size = 32.dp),
                )
            },
            selected = activeTab == HomeScreenTab.MAIN,
            onClick = { onTabClick.invoke(HomeScreenTab.MAIN) },
            alwaysShowLabel = false,
            modifier = modifier,
        )

        NavigationBarItem(
            label = { Text("Trainings") },
            icon = {
                Icon(
                    painter = painterResource(resource = Res.drawable.tab_trainings),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(size = 32.dp),
                )
            },
            selected = activeTab == HomeScreenTab.TRAININGS,
            onClick = { onTabClick.invoke(HomeScreenTab.TRAININGS) },
            alwaysShowLabel = false,
            modifier = modifier,
        )

        NavigationBarItem(
            label = { Text("Progress") },
            icon = {
                Icon(
                    painter = painterResource(resource = Res.drawable.tab_progress),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(size = 32.dp),
                )
            },
            selected = activeTab == HomeScreenTab.PROGRESS,
            onClick = { onTabClick.invoke(HomeScreenTab.PROGRESS) },
            alwaysShowLabel = false,
            modifier = modifier,
        )

        NavigationBarItem(
            label = { Text("Reminders") },
            icon = {
                Icon(
                    painter = painterResource(resource = Res.drawable.tab_reminders),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(size = 32.dp),
                )
            },
            selected = activeTab == HomeScreenTab.REMINDERS,
            onClick = { onTabClick.invoke(HomeScreenTab.REMINDERS) },
            alwaysShowLabel = false,
            modifier = modifier,
        )
    }
}
