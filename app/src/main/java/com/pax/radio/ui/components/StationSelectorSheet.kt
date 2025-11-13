package com.pax.radio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.pax.radio.data.RadioStation
import com.pax.radio.ui.theme.CardBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelectorSheet(
    stations: List<RadioStation>,
    currentStation: RadioStation?,
    onStationSelect: (RadioStation) -> Unit,
    onDismiss: () -> Unit
) {
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStations = remember(stations, searchQuery) {
        if (searchQuery.isEmpty()) {
            stations
        } else {
            stations.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black.copy(alpha = 0.5f),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with title and search
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = !searchExpanded,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        Text(
                            text = "Выбор станции",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    AnimatedVisibility(
                        visible = searchExpanded,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally(),
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Поиск станции...", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            searchExpanded = !searchExpanded
                            if (!searchExpanded) {
                                searchQuery = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchExpanded) "Закрыть поиск" else "Открыть поиск",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredStations, key = { it.id }) { station ->
                        StationCard(
                            station = station,
                            isSelected = station.id == currentStation?.id,
                            onClick = { onStationSelect(station) }
                        )
                    }

                    if (filteredStations.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Станции не найдены",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationCard(
    station: RadioStation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isInvalid = !station.isValidUrl

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = when {
            isInvalid -> Color(0xFF3A1A1A) // Red tint for invalid
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else -> CardBackground
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Station Logo
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(station.imageUrl?.let { "file:///android_asset/$it" })
                    .crossfade(true)
                    .build(),
                contentDescription = station.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isInvalid) Color(0xFF5A3A3A) else Color(0xFF3A3A3A)),
                contentScale = ContentScale.Crop,
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isInvalid) Color(0xFF5A3A3A) else Color(0xFF3A3A3A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = if (isInvalid) Color(0xFFFF6666) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isInvalid) Color(0xFFFF6666) else Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = if (isInvalid) "No stream available" else station.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isInvalid) Color(0xFFCC6666) else Color(0xFFB0B0B0)
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Filled.Equalizer,
                    contentDescription = "Playing",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else if (isInvalid) {
                Icon(
                    Icons.Filled.ErrorOutline,
                    contentDescription = "Invalid",
                    tint = Color(0xFFFF6666),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
