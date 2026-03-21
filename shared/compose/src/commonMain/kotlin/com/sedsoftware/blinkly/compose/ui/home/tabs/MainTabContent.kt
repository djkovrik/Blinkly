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
import com.sedsoftware.blinkly.component.main.MainTabComponent

@Composable
fun MainTabContent(
    component: MainTabComponent,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Main tab")
        Button(onClick = component::onPreferencesClick, modifier = Modifier.padding(all = 8.dp)) {
            Text("Open prefs")
        }
    }
}
