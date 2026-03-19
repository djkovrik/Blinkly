package com.sedsoftware.blinkly.compose.ui.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sedsoftware.blinkly.component.home.HomeScreenComponent

@Composable
fun HomeScreenContent(
    component: HomeScreenComponent,
    modifier: Modifier = Modifier,
) {
    Text("Home screen")
}
