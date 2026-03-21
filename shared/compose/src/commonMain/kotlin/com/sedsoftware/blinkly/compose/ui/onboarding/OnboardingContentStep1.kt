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
import com.sedsoftware.blinkly.component.step1.OnboardingStep1Component

@Composable
fun OnboardingContentStep1(
    component: OnboardingStep1Component,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        Text("Step 1")
        Button(onClick = component::nextStep, modifier = Modifier.padding(all = 8.dp)) {
            Text("Next")
        }
    }
}
