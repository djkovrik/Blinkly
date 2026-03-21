package com.sedsoftware.blinkly.compose.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.PredictiveBackParams
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.materialPredictiveBackAnimatable
import com.sedsoftware.blinkly.component.root.RootComponent
import com.sedsoftware.blinkly.compose.theme.BlinklyAppTheme
import com.sedsoftware.blinkly.compose.ui.achievements.AchievementsContent
import com.sedsoftware.blinkly.compose.ui.exercises.BlockAContent
import com.sedsoftware.blinkly.compose.ui.exercises.BlockBContent
import com.sedsoftware.blinkly.compose.ui.exercises.BlockCContent
import com.sedsoftware.blinkly.compose.ui.garden.GardenContent
import com.sedsoftware.blinkly.compose.ui.home.HomeScreenContent
import com.sedsoftware.blinkly.compose.ui.newreminder.AddNewReminderContent
import com.sedsoftware.blinkly.compose.ui.onboarding.OnboardingContent
import com.sedsoftware.blinkly.compose.ui.preferences.PreferencesContent

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
) {
    BlinklyAppTheme(onThemeChanged) {
        ChildStack(
            stack = component.childStack,
            animation = stackAnimation(
                animator = fade() + scale(),
                predictiveBackParams = {
                    PredictiveBackParams(
                        backHandler = component.backHandler,
                        onBack = component::onBack,
                        animatable = ::materialPredictiveBackAnimatable,
                    )
                }
            ),
            modifier = modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            when (val child = it.instance) {
                is RootComponent.Child.Onboarding -> OnboardingContent(child.component)
                is RootComponent.Child.HomeScreen -> HomeScreenContent(child.component)
                is RootComponent.Child.Preferences -> PreferencesContent(child.component)
                is RootComponent.Child.BlockA -> BlockAContent(child.component)
                is RootComponent.Child.BlockB -> BlockBContent(child.component)
                is RootComponent.Child.BlockC -> BlockCContent(child.component)
                is RootComponent.Child.Achievements -> AchievementsContent(child.component)
                is RootComponent.Child.Garden -> GardenContent(child.component)
                is RootComponent.Child.AddNewReminder -> AddNewReminderContent(child.component)
            }
        }
    }
}
