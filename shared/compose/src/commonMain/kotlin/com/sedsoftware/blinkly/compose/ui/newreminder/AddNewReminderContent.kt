package com.sedsoftware.blinkly.compose.ui.newreminder

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
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent

@Composable
fun AddNewReminderContent(
    component: AddNewReminderComponent,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Add new reminder")
        Button(onClick = component::onBackClick, modifier = Modifier.padding(all = 8.dp)) {
            Text("Close")
        }
    }
}
