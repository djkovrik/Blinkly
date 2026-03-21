package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Text("Step 5")
        Button(onClick = component::previousStep, modifier = Modifier.padding(all = 8.dp)) {
            Text("Previous")
        }
        Button(onClick = component::nextStep, modifier = Modifier.padding(all = 8.dp)) {
            Text("Home screen")
        }
    }
}
