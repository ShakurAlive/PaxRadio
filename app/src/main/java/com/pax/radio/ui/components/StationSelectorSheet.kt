package com.pax.radio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import com.pax.radio.data.DisplayableItem
import com.pax.radio.data.RadioGroup
import com.pax.radio.data.RadioStation
import com.pax.radio.ui.theme.CardBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelectorSheet(
    stations: List<DisplayableItem>,
    currentStation: RadioStation?,
    onStationSelect: (RadioStation) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleGroup: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val filteredStations = remember(stations, searchQuery) {
        if (searchQuery.isEmpty()) {
            stations
        } else {
            stations.mapNotNull { item ->
                when (item) {
                    is RadioStation -> if (item.name.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)) item else null
                    is RadioGroup -> {
                        val filtered = item.stations.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        if (filtered.isNotEmpty()) item.copy(stations = filtered, isExpanded = true) else null
                    }
                    else -> null
                }
            }
        }
    }

    LaunchedEffect(currentStation, filteredStations) {
        coroutineScope.launch {
            val index = filteredStations.indexOfFirst {
                (it is RadioStation && it.id == currentStation?.id) ||
                (it is RadioGroup && it.stations.any { s -> s.id == currentStation?.id })
            }
            if (index != -1) {
                listState.animateScrollToItem(index)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    AnimatedVisibility(
                        visible = searchExpanded,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Поиск...") },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    IconButton(onClick = { searchExpanded = !searchExpanded }) {
                        Icon(
                            imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    } else {
                        items(
                            items = filteredStations,
                            key = { it.id }
                        ) { item ->
                            when (item) {
                                is RadioGroup -> {
                                    GroupItem(
                                        group = item,
                                        onToggleGroup = onToggleGroup,
                                        onStationSelect = onStationSelect,
                                        currentStation = currentStation,
                                        onToggleFavorite = onToggleFavorite
                                    )
                                }
                                is RadioStation -> {
                                    StationItem(
                                        station = item,
                                        isSelected = item.id == currentStation?.id,
                                        onStationSelect = { onStationSelect(item) },
                                        onToggleFavorite = { onToggleFavorite(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupItem(
    group: RadioGroup,
    onToggleGroup: (String) -> Unit,
    onStationSelect: (RadioStation) -> Unit,
    currentStation: RadioStation?,
    onToggleFavorite: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleGroup(group.id) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(group.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = group.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                loading = {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Loading",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                error = {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        AnimatedVisibility(visible = group.isExpanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                group.stations.forEach { station ->
                    StationItem(
                        station = station,
                        isSelected = station.id == currentStation?.id,
                        onStationSelect = { onStationSelect(station) },
                        onToggleFavorite = { onToggleFavorite(station.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StationItem(
    station: RadioStation,
    isSelected: Boolean,
    onStationSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStationSelect)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(station.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = station.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp)),
            loading = {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Loading",
                    tint = contentColor
                )
            },
            error = {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
            Text(
                text = station.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Equalizer,
                contentDescription = "Playing",
                tint = contentColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (station.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorite",
                tint = if (station.isFavorite) MaterialTheme.colorScheme.secondary else contentColor.copy(alpha = 0.7f)
            )
        }
    }
}
