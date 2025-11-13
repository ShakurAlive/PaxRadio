package com.pax.radio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TimeInputScreen(
    title: String,
    onTimeSet: (hours: Int, minutes: Int) -> Unit
) {
    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title)
        Spacer(modifier = Modifier.height(16.dp))
        TimeInputBox(
            label = "Hours",
            value = hours,
            onValueChange = { hours = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TimeInputBox(
            label = "Minutes",
            value = minutes,
            onValueChange = { minutes = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onTimeSet(hours.toIntOrNull() ?: 0, minutes.toIntOrNull() ?: 0)
        }) {
            Text("Set")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    TimeInputScreen(title = "Set Alarm", onTimeSet = { _, _ -> })
}

@Preview(showBackground = true)
@Composable
fun SleepTimerScreenPreview() {
    TimeInputScreen(title = "Set Sleep Timer", onTimeSet = { _, _ -> })
}

