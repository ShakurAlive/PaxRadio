package com.example.paxradio.ui.components

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paxradio.data.RadioStation
import com.example.paxradio.ui.theme.CardBackground
import com.example.paxradio.ui.theme.DeepBlue
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepAlarmBottomSheet(
    currentStation: RadioStation?,
    stations: List<RadioStation>,
    onDismiss: () -> Unit,
    onSleepTimerSet: (Long) -> Unit,
    onAlarmSet: (Int, Int, RadioStation) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Sleep & Alarm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardBackground,
                contentColor = DeepBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sleep Timer") },
                    icon = { Icon(Icons.Filled.Bedtime, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Alarm") },
                    icon = { Icon(Icons.Filled.Alarm, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTab) {
                0 -> SleepTimerTab(onTimerSet = { minutes ->
                    onSleepTimerSet(minutes * 60 * 1000L)
                    onDismiss()
                })
                1 -> AlarmTab(
                    currentStation = currentStation,
                    stations = stations,
                    onAlarmSet = { hour, minute, station ->
                        onAlarmSet(hour, minute, station)
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SleepTimerTab(onTimerSet: (Long) -> Unit) {
    var customMinutes by remember { mutableStateOf(30) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Turn off playback after:",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        // Preset buttons
        val presets = listOf(15, 30, 45, 60)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { minutes ->
                Button(
                    onClick = { onTimerSet(minutes.toLong()) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardBackground
                    )
                ) {
                    Text("${minutes}m")
                }
            }
        }

        HorizontalDivider(color = Color(0xFF3A3A3A), modifier = Modifier.padding(vertical = 8.dp))

        // Custom time
        Text(
            text = "Custom time:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$customMinutes minutes",
                style = MaterialTheme.typography.titleMedium,
                color = DeepBlue
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { if (customMinutes > 5) customMinutes -= 5 }
                ) {
                    Text("-", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                }

                IconButton(
                    onClick = { if (customMinutes < 180) customMinutes += 5 }
                ) {
                    Text("+", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        Slider(
            value = customMinutes.toFloat(),
            onValueChange = { customMinutes = it.toInt() },
            valueRange = 5f..180f,
            steps = 35,
            colors = SliderDefaults.colors(
                thumbColor = DeepBlue,
                activeTrackColor = DeepBlue
            )
        )

        Button(
            onClick = { onTimerSet(customMinutes.toLong()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Set Sleep Timer", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = { onTimerSet(0) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text("Cancel Timer")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmTab(
    currentStation: RadioStation?,
    stations: List<RadioStation>,
    onAlarmSet: (Int, Int, RadioStation) -> Unit
) {
    var selectedHour by remember { mutableStateOf(7) }
    var selectedMinute by remember { mutableStateOf(30) }
    var selectedStation by remember { mutableStateOf(currentStation ?: stations.firstOrNull()) }
    var repeat by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Wake up at:",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        // Time Picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour picker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { selectedHour = (selectedHour + 1) % 24 }) {
                    Text("▲", color = DeepBlue, style = MaterialTheme.typography.headlineMedium)
                }

                Surface(
                    modifier = Modifier
                        .width(80.dp)
                        .height(60.dp),
                    color = CardBackground,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = String.format("%02d", selectedHour),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = { selectedHour = if (selectedHour == 0) 23 else selectedHour - 1 }) {
                    Text("▼", color = DeepBlue, style = MaterialTheme.typography.headlineMedium)
                }
            }

            Text(
                text = ":",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Minute picker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { selectedMinute = (selectedMinute + 5) % 60 }) {
                    Text("▲", color = DeepBlue, style = MaterialTheme.typography.headlineMedium)
                }

                Surface(
                    modifier = Modifier
                        .width(80.dp)
                        .height(60.dp),
                    color = CardBackground,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = String.format("%02d", selectedMinute),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = { selectedMinute = if (selectedMinute == 0) 55 else selectedMinute - 5 }) {
                    Text("▼", color = DeepBlue, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        HorizontalDivider(color = Color(0xFF3A3A3A))

        // Station selection
        Text(
            text = "Play station:",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStation?.name ?: "Select station",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DeepBlue,
                    unfocusedBorderColor = Color(0xFF3A3A3A)
                ),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                stations.filter { it.isValidUrl }.forEach { station ->
                    DropdownMenuItem(
                        text = { Text(station.name) },
                        onClick = {
                            selectedStation = station
                            expanded = false
                        }
                    )
                }
            }
        }

        // Repeat option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Repeat daily",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Switch(
                checked = repeat,
                onCheckedChange = { repeat = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DeepBlue,
                    checkedTrackColor = DeepBlue.copy(alpha = 0.5f)
                )
            )
        }

        Button(
            onClick = {
                selectedStation?.let { station ->
                    onAlarmSet(selectedHour, selectedMinute, station)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepBlue
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = selectedStation != null
        ) {
            Icon(Icons.Filled.Alarm, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Set Alarm", fontWeight = FontWeight.Bold)
        }
    }
}

