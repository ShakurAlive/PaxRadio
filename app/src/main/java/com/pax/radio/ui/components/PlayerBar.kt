package com.pax.radio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pax.radio.data.RadioStation

@Composable
fun PlayerBar(
    current: RadioStation?,
    playing: Boolean,
    volume: Float,
    onToggle: () -> Unit,
    onVolume: (Float) -> Unit
) {
    AnimatedVisibility(visible = current != null) {
        Surface(tonalElevation = 4.dp) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(current?.name ?: "", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onToggle) {
                        Icon(if (playing) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle, contentDescription = "toggle")
                    }
                }
                Slider(value = volume, onValueChange = onVolume, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

