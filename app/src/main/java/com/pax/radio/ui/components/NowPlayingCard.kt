package com.pax.radio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.pax.radio.data.PlayerState
import com.pax.radio.data.RadioStation
import com.pax.radio.ui.theme.CardBackground

@Composable
fun NowPlayingCard(
    station: RadioStation?,
    trackTitle: String?,
    playerState: PlayerState,
    modifier: Modifier = Modifier,
    onToggleFavorite: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.6f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Station Logo
                StationLogo(
                    imageUrl = station?.imageUrl,
                    stationName = station?.name ?: "Select Station"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Station Name
                val stationName = station?.name ?: "Станция не выбрана"
                val nameParts = stationName.split(" (", limit = 2)
                val mainName = nameParts.getOrNull(0) ?: stationName
                val region = if (nameParts.size > 1) "(${nameParts[1]}" else null

                Text(
                    text = mainName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 28.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                region?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Track info
                val statusText = when (playerState) {
                    is PlayerState.Playing -> trackTitle ?: "Radio Stream"
                    is PlayerState.Buffering -> "Buffering..."
                    is PlayerState.Error -> "Playback Error"
                    is PlayerState.NoStream -> "Stream not available"
                    else -> "Stopped"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0B0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorite button (top right corner)
            if (station != null) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (station.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (station.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (station.isFavorite) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StationLogo(
    imageUrl: String?,
    stationName: String
) {
    val context = LocalContext.current
    val imageRequest = remember(imageUrl) {
        val imagePath = imageUrl?.let { "file:///android_asset/$it" }
            ?: "file:///android_asset/radio_assets/logos/default.png"
        ImageRequest.Builder(context)
            .data(imagePath)
            .crossfade(true)
            .build()
    }

    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = stationName,
        modifier = Modifier.size(120.dp).clip(RoundedCornerShape(24.dp)),
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
                    color = MaterialTheme.colorScheme.primary
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
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
