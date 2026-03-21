package com.sedsoftware.blinkly.compose.ui.home.tabs

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
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent

@Composable
fun ProgressTabContent(
    component: ProgressTabComponent,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Progress tab")
        Button(onClick = component::onAchievementsClick, modifier = Modifier.padding(all = 8.dp)) {
            Text("Achievements")
        }
        Button(onClick = component::onGardenClick, modifier = Modifier.padding(all = 8.dp)) {
            Text("Garden")
        }
    }
}
