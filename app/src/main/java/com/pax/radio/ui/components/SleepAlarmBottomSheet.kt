package com.pax.radio.ui.components

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
import androidx.compose.ui.unit.dp
import com.pax.radio.data.RadioStation
import com.pax.radio.ui.theme.CardBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepAlarmBottomSheet(
    currentStation: RadioStation?,
    stations: List<RadioStation>,
    onDismiss: () -> Unit,
    onSleepTimerSet: (Long) -> Unit,
    onAlarmSet: (Int, Int, RadioStation) -> Unit,
    onAlarmCancel: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black.copy(alpha = 0.5f),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardBackground,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Таймер сна") },
                        icon = { Icon(Icons.Filled.Bedtime, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Будильник") },
                        icon = { Icon(Icons.Filled.Alarm, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (selectedTab) {
                    0 -> SleepTimerTab(
                        onTimerSet = onSleepTimerSet,
                        onDismiss = onDismiss
                    )
                    1 -> AlarmTab(
                        currentStation = currentStation,
                        stations = stations,
                        onAlarmSet = { hour, minute, station ->
                            onAlarmSet(hour, minute, station)
                            onDismiss()
                        },
                        onAlarmCancel = {
                            onAlarmCancel()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SleepTimerTab(onTimerSet: (Long) -> Unit, onDismiss: () -> Unit) {
    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("30") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Остановить воспроизведение через:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeInputBox(
                label = "Часы",
                value = hours,
                onValueChange = { hours = it },
                modifier = Modifier.width(90.dp)
            )
            Text(":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurface)
            TimeInputBox(
                label = "Минуты",
                value = minutes,
                onValueChange = { minutes = it },
                modifier = Modifier.width(90.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onDismiss() },
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Отмена")
            }
            Button(
                onClick = {
                    val h = hours.toLongOrNull() ?: 0L
                    val m = minutes.toLongOrNull() ?: 0L
                    val totalMillis = (h * 3600 + m * 60) * 1000L
                    onTimerSet(totalMillis)
                    onDismiss()
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Bedtime, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сохранить", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmTab(
    currentStation: RadioStation?,
    stations: List<RadioStation>,
    onAlarmSet: (Int, Int, RadioStation) -> Unit,
    onAlarmCancel: () -> Unit
) {
    var hours by remember { mutableStateOf("07") }
    var minutes by remember { mutableStateOf("30") }

    var selectedStation by remember { mutableStateOf(currentStation ?: stations.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Установить будильник на:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeInputBox(
                label = "Часы",
                value = hours,
                onValueChange = { hours = it },
                modifier = Modifier.width(90.dp)
            )
            Text(":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurface)
            TimeInputBox(
                label = "Минуты",
                value = minutes,
                onValueChange = { minutes = it },
                modifier = Modifier.width(90.dp)
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

        Text(
            text = "Выбор радиостанции:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStation?.name ?: "Select station",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onAlarmCancel,
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Отмена")
            }
            Button(
                onClick = {
                    selectedStation?.let { station ->
                        onAlarmSet(hours.toIntOrNull() ?: 0, minutes.toIntOrNull() ?: 0, station)
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = selectedStation != null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сохранить", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
