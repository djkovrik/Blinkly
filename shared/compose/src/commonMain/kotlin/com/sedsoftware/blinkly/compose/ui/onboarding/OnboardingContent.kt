package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent

@Composable
fun OnboardingContent(
    component: OnboardingComponent,
    modifier: Modifier = Modifier,
) {
    Text("Onboarding")
}
