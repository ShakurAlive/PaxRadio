package com.example.paxradio.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paxradio.ui.components.SleepAlarmBottomSheet
import com.example.paxradio.ui.components.*
import com.example.paxradio.ui.fm.FmModeScreen
import com.example.paxradio.ui.streaming.StreamingViewModel
import com.example.paxradio.ui.theme.DarkBackground
import com.example.paxradio.ui.theme.DeepBlue
import com.example.paxradio.ui.theme.PaxRadioTheme
import com.example.paxradio.player.RadioPlaybackService
import com.example.paxradio.ui.fm.FmRadioViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, RadioPlaybackService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        setContent { AppContent() }
    }
}

@Composable
private fun AppContent() {
    PaxRadioTheme {
        val streamingVm: StreamingViewModel = hiltViewModel()
        val fmVm: FmRadioViewModel = hiltViewModel()

        var showStationSelector by remember { mutableStateOf(false) }
        var showSleepAlarm by remember { mutableStateOf(false) }
        var fmMode by remember { mutableStateOf(false) }

        val current by streamingVm.current.collectAsState()
        val playerState by streamingVm.playerState.collectAsState()
        val stations by streamingVm.stationsFlow.collectAsState()
        val sleepTimerActive by streamingVm.sleepTimerActive.collectAsState()
        val sleepTimerMinutes by streamingVm.sleepTimerMinutes.collectAsState()
        val frequency by fmVm.freq.collectAsState()
        val headphones by fmVm.headphones.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            if (fmMode) {
                // FM Mode
                FmModeScreen(
                    hasHeadphones = headphones,
                    frequency = frequency,
                    onFrequencyChange = { fmVm.setFreq(it) },
                    onScan = { fmVm.scan() }
                )
            } else {
                // Radio Mode
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Now Playing Card
                    NowPlayingCard(
                        station = current,
                        playerState = playerState,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Sleep Timer Status
                    if (sleepTimerActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⏱ Sleeping in ${sleepTimerMinutes}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepBlue,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Play/Pause Button
                    PlayPauseButton(
                        playerState = playerState,
                        onClick = { streamingVm.toggle() },
                        enabled = current != null && current!!.isValidUrl
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Volume Knob
                    VolumeKnob(
                        volume = streamingVm.volume(),
                        onVolumeChange = { streamingVm.setVolume(it) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Bottom Bar
            BottomActionBar(
                onListClick = { showStationSelector = true },
                onSleepAlarmClick = { showSleepAlarm = true },
                onFmClick = {
                    fmMode = !fmMode
                    fmVm.refreshHeadphones()
                },
                fmMode = fmMode,
                sleepTimerActive = sleepTimerActive,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Station Selector Sheet
        if (showStationSelector) {
            StationSelectorSheet(
                stations = stations,
                currentStation = current,
                onStationSelect = {
                    streamingVm.select(it)
                    showStationSelector = false
                },
                onDismiss = { showStationSelector = false }
            )
        }

        // Sleep & Alarm Sheet
        if (showSleepAlarm) {
            SleepAlarmBottomSheet(
                currentStation = current,
                stations = stations,
                onDismiss = { showSleepAlarm = false },
                onSleepTimerSet = { minutes ->
                    streamingVm.setSleepTimer(minutes)
                },
                onAlarmSet = { hour, minute, station ->
                    streamingVm.setAlarm(hour, minute, station)
                }
            )
        }
    }
}

@Composable
private fun PlayPauseButton(
    playerState: com.example.paxradio.data.PlayerState,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val isPlaying = playerState is com.example.paxradio.data.PlayerState.Playing
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "button_scale"
    )

    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(100.dp)
            .scale(scale),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (enabled) DeepBlue else Color(0xFF3A3A3A),
            disabledContainerColor = Color(0xFF3A3A3A)
        ),
        shape = CircleShape
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(60.dp),
            tint = if (enabled) Color.White else Color.Gray
        )
    }
}

@Composable
private fun BottomActionBar(
    onListClick: () -> Unit,
    onSleepAlarmClick: () -> Unit,
    onFmClick: () -> Unit,
    fmMode: Boolean,
    sleepTimerActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.Black
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // List Icon
            IconButton(
                onClick = onListClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = "Station List",
                    tint = if (!fmMode) DeepBlue else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Sleep/Alarm Icon
            IconButton(
                onClick = onSleepAlarmClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Filled.Bedtime,
                    contentDescription = "Sleep & Alarm",
                    tint = if (sleepTimerActive) DeepBlue else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }

            // FM Icon
            IconButton(
                onClick = onFmClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Filled.Sensors,
                    contentDescription = "FM Radio",
                    tint = if (fmMode) DeepBlue else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

