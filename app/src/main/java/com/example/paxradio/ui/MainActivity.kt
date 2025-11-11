package com.example.paxradio.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paxradio.player.RadioPlaybackService
import com.example.paxradio.player.sound.SoundPlayer
import com.example.paxradio.ui.components.*
import com.example.paxradio.ui.fm.FmModeScreen
import com.example.paxradio.ui.fm.FmRadioViewModel
import com.example.paxradio.ui.streaming.StreamingViewModel
import com.example.paxradio.ui.theme.DarkBackground
import com.example.paxradio.ui.theme.DeepBlue
import com.example.paxradio.ui.theme.PaxRadioTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundPlayer: SoundPlayer
    private val streamingVm: StreamingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, RadioPlaybackService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        setContent { AppContent(soundPlayer, streamingVm) }
    }
}

@Composable
private fun AppContent(soundPlayer: SoundPlayer, streamingVm: StreamingViewModel) {
    PaxRadioTheme {
        val fmVm: FmRadioViewModel = hiltViewModel()
        val haptic = LocalHapticFeedback.current

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
        val volume by streamingVm.volume.collectAsState()
        var lastVolume by rememberSaveable { mutableFloatStateOf(0f) }


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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Now Playing Card
                    NowPlayingCard(
                        station = current,
                        playerState = playerState,
                        onToggle = { streamingVm.toggle() },
                        enabled = current != null && current!!.isValidUrl,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Sleep Timer Status
                    if (sleepTimerActive) {
                        Text(
                            text = "⏱ Sleeping in ${sleepTimerMinutes}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepBlue,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }


                    // Volume Knob
                    VolumeKnob(
                        volume = volume,
                        onVolumeChange = { newVolume ->
                            // Trigger haptics and sound on discrete steps (e.g., every 2%)
                            if (kotlin.math.abs(newVolume - lastVolume) >= 0.02f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                soundPlayer.playClick()
                                lastVolume = newVolume
                            }
                            streamingVm.setVolume(newVolume)
                        },
                        modifier = Modifier.padding(bottom = 100.dp)
                    )
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
