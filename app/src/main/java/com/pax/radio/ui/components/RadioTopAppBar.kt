package com.pax.radio.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RadioTopAppBar(
    isPlaying: Boolean,
    sleepTimerActive: Boolean,
    onSleepAlarmClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFmMode: Boolean = false,
    onFmClick: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FM Radio button (left) - фиксированная ширина
                Box(
                    modifier = Modifier.width(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onFmClick) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "FM Radio",
                            tint = if (isFmMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Live Badge - центрированный с weight
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        LiveBadge()
                    }
                }

                // Action Icons - фиксированная ширина справа (2 кнопки по 48dp)
                Box(
                    modifier = Modifier.width(96.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onSleepAlarmClick) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep & Alarm",
                            tint = if (sleepTimerActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    Surface(
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = Color.Red
    ) {
        Text(
            text = "● LIVE",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
