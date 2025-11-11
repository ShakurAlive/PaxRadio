package com.example.paxradio.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.paxradio.data.RadioStation
import com.example.paxradio.ui.theme.CardBackground
import com.example.paxradio.ui.theme.DeepBlue

@Composable
fun NowPlayingCard(
    station: RadioStation?,
    playerState: com.example.paxradio.data.PlayerState,
    onToggle: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val isPlaying = playerState is com.example.paxradio.data.PlayerState.Playing
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LIVE Badge with pulse animation
            if (isPlaying) {
                LiveBadge()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Station Logo
            StationLogo(
                imageUrl = station?.imageUrl,
                stationName = station?.name ?: "Select Station"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Station Name
            Text(
                text = station?.name ?: "No Station Playing",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Track info
            Text(
                text = when (playerState) {
                    is com.example.paxradio.data.PlayerState.Playing -> "Radio Stream"
                    is com.example.paxradio.data.PlayerState.Paused -> "Paused"
                    is com.example.paxradio.data.PlayerState.NoStream -> "Stream not available"
                    is com.example.paxradio.data.PlayerState.Error -> "Playback error"
                    else -> "Not Playing"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (playerState) {
                    is com.example.paxradio.data.PlayerState.NoStream,
                    is com.example.paxradio.data.PlayerState.Error -> Color(0xFFFF6666)
                    else -> Color(0xFFB0B0B0)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Play/Pause Button
            FilledIconButton(
                onClick = onToggle,
                enabled = enabled,
                modifier = Modifier
                    .size(80.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (enabled) DeepBlue else Color(0xFF3A3A3A),
                    disabledContainerColor = Color(0xFF3A3A3A)
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(50.dp),
                    tint = if (enabled) Color.White else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
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

@Composable
private fun StationLogo(
    imageUrl: String?,
    stationName: String
) {
    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl?.let { "file:///android_asset/$it" })
            .crossfade(true)
            .build(),
        contentDescription = stationName,
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3A3A3A),
                                Color(0xFF2A2A2A)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = DeepBlue
                )
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF3A3A3A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = DeepBlue
                )
            }
        }
    )
}
