package com.example.paxradio.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paxradio.data.RadioStation
import com.example.paxradio.ui.theme.CardBackground
import kotlinx.coroutines.launch
import kotlin.math.abs

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
                        onSleepTimerSet(minutes) // Corrected call
                        onDismiss()
                    })
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
private fun SleepTimerTab(onTimerSet: (Long) -> Unit) {
    val hours = (0..3).map { "%02d".format(it) }
    val minutes = (0..59).map { "%02d".format(it) }

    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(30) }

    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2 % hours.size) + 0)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2 % minutes.size) + 30)

    LaunchedEffect(hourListState.isScrollInProgress) {
        if (!hourListState.isScrollInProgress) {
            val center = hourListState.layoutInfo.viewportEndOffset / 2
            val closestItem = hourListState.layoutInfo.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }
            closestItem?.let {
                selectedHour = it.index % hours.size
            }
        }
    }

    LaunchedEffect(minuteListState.isScrollInProgress) {
        if (!minuteListState.isScrollInProgress) {
            val center = minuteListState.layoutInfo.viewportEndOffset / 2
            val closestItem = minuteListState.layoutInfo.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }
            closestItem?.let {
                selectedMinute = it.index % minutes.size
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Turn off playback after:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeWheelPicker(items = hours, state = hourListState, onValueChange = { selectedHour = it })
            Text(":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurface)
            TimeWheelPicker(items = minutes, state = minuteListState, onValueChange = { selectedMinute = it })
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onTimerSet(0) },
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val totalMinutes = selectedHour * 60L + selectedMinute
                    val totalMillis = totalMinutes * 60000L
                    onTimerSet(totalMillis)
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Bedtime, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save", color = MaterialTheme.colorScheme.onPrimary)
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
    val hours = (0..23).map { "%02d".format(it) }
    val minutes = (0..59).map { "%02d".format(it) }
    
    var selectedHour by remember { mutableStateOf(7) }
    var selectedMinute by remember { mutableStateOf(30) }

    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2 % hours.size) + 7)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2 % minutes.size) + 30)

    LaunchedEffect(hourListState.isScrollInProgress) {
        if (!hourListState.isScrollInProgress) {
            val center = hourListState.layoutInfo.viewportEndOffset / 2
            val closestItem = hourListState.layoutInfo.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }
            closestItem?.let {
                selectedHour = it.index % hours.size
            }
        }
    }

    LaunchedEffect(minuteListState.isScrollInProgress) {
        if (!minuteListState.isScrollInProgress) {
            val center = minuteListState.layoutInfo.viewportEndOffset / 2
            val closestItem = minuteListState.layoutInfo.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }
            closestItem?.let {
                selectedMinute = it.index % minutes.size
            }
        }
    }
    
    var selectedStation by remember { mutableStateOf(currentStation ?: stations.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeWheelPicker(items = hours, state = hourListState, onValueChange = { selectedHour = it })
            Text(":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurface)
            TimeWheelPicker(items = minutes, state = minuteListState, onValueChange = { selectedMinute = it })
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

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
                Text("Cancel")
            }
            Button(
                onClick = {
                    selectedStation?.let { station ->
                        onAlarmSet(selectedHour, selectedMinute, station)
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = selectedStation != null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
