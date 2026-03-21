package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component

@Composable
fun OnboardingContentStep5(
    component: OnboardingStep5Component,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        Text("Step 2")
        Button(onClick = component::previousStep) {
            Text("Previous")
        }
        Button(onClick = component::nextStep) {
            Text("Home screen")
        }
    }
}
