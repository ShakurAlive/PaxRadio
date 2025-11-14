package com.pax.radio.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pax.radio.R
import com.pax.radio.data.AppTheme
import com.pax.radio.data.PlayerState
import com.pax.radio.player.RadioPlaybackService
import com.pax.radio.player.sound.SoundPlayer
import com.pax.radio.ui.components.*
import com.pax.radio.ui.fm.FmModeScreen
import com.pax.radio.ui.fm.FmRadioViewModel
import com.pax.radio.ui.settings.SettingsScreen
import com.pax.radio.ui.settings.SettingsViewModel
import com.pax.radio.ui.streaming.StreamingViewModel
import com.pax.radio.ui.theme.PaxRadioTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundPlayer: SoundPlayer
    private val streamingVm: StreamingViewModel by viewModels()

    private val playbackStateReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == com.pax.radio.player.RadioPlaybackService.ACTION_PLAYBACK_STATE_CHANGED) {
                val stationId = intent.getStringExtra(com.pax.radio.player.RadioPlaybackService.EXTRA_STATION_ID) ?: ""
                val stationUrl = intent.getStringExtra(com.pax.radio.player.RadioPlaybackService.EXTRA_STATION_URL) ?: ""
                val stationName = intent.getStringExtra("station_name") ?: ""
                val isPlaying = intent.getBooleanExtra("is_playing", false)

                android.util.Log.d("MainActivity", "Broadcast received: $stationName, playing=$isPlaying")

                if (isPlaying) {
                    streamingVm.syncPlayerState(stationId, stationUrl, stationName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, RadioPlaybackService::class.java)
        startService(serviceIntent)

        // Register broadcast receiver
        val filter = android.content.IntentFilter(com.pax.radio.player.RadioPlaybackService.ACTION_PLAYBACK_STATE_CHANGED)
        ContextCompat.registerReceiver(
            this,
            playbackStateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        setContent { AppContent(soundPlayer, streamingVm) }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(playbackStateReceiver)
    }
}

@Composable
private fun AppContent(soundPlayer: SoundPlayer, streamingVm: StreamingViewModel) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val theme by settingsViewModel.theme.collectAsState()

    PaxRadioTheme(appTheme = theme) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                MainRadioScreen(
                    soundPlayer = soundPlayer,
                    streamingVm = streamingVm,
                    theme = theme,
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainRadioScreen(
    soundPlayer: SoundPlayer,
    streamingVm: StreamingViewModel,
    theme: AppTheme,
    onNavigateToSettings: () -> Unit
) {
    val fmVm: FmRadioViewModel = hiltViewModel()
    var showStationSelector by remember { mutableStateOf(false) }
    var showSleepAlarm by remember { mutableStateOf(false) }
    var fmMode by remember { mutableStateOf(false) }

    val current by streamingVm.current.collectAsState()
    val playerState by streamingVm.playerState.collectAsState()
    val stations by streamingVm.stationsFlow.collectAsState()
    val sleepTimerActive by streamingVm.sleepTimerActive.collectAsState()
    val trackTitle by streamingVm.trackTitle.collectAsState()
    val frequency by fmVm.freq.collectAsState()
    val headphones by fmVm.headphones.collectAsState()
    val isPlaying = playerState is PlayerState.Playing

    Box(modifier = Modifier.fillMaxSize()) {
        if (theme == AppTheme.BORDEAUX) {
            Image(
                painter = painterResource(id = R.drawable.bg_bordeaux),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Scaffold(
            topBar = {
                RadioTopAppBar(
                    modifier = Modifier.padding(50.dp),
                    isPlaying = isPlaying,
                    sleepTimerActive = sleepTimerActive,
                    onSleepAlarmClick = { showSleepAlarm = true },
                    onSettingsClick = onNavigateToSettings
                )
            },
            bottomBar = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    BottomActionBar(
                        modifier = Modifier.padding(bottom = 50.dp),
                        isPlaying = isPlaying,
                        isFmMode = fmMode,
                        onListClick = { showStationSelector = true },
                        onPlayPauseClick = { streamingVm.toggle() },
                        onFmClick = {
                            fmMode = !fmMode
                            fmVm.refreshHeadphones()
                        },
                        isPlayPauseEnabled = current != null && current!!.isValidUrl
                    )
                }
            },
            containerColor = if (theme == AppTheme.BORDEAUX) Color.Transparent else MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                if (fmMode) {
                    FmModeScreen(
                        hasHeadphones = headphones,
                        frequency = frequency,
                        onFrequencyChange = { fmVm.setFreq(it) },
                        onScan = { fmVm.scan() }
                    )
                } else {
                    NowPlayingCard(
                        station = current,
                        trackTitle = trackTitle
                    )
                    AudioVisualizer(isPlaying = isPlaying)
                }
            }
        }
    }

    if (showStationSelector) {
        StationSelectorSheet(
            stations = stations,
            currentStation = current,
            onStationSelect = {
                streamingVm.select(it)
                showStationSelector = false
            },
            onToggleFavorite = { stationId ->
                streamingVm.toggleFavorite(stationId)
            },
            onDismiss = { showStationSelector = false }
        )
    }

    if (showSleepAlarm) {
        SleepAlarmBottomSheet(
            currentStation = current,
            stations = stations,
            onDismiss = { showSleepAlarm = false },
            onSleepTimerSet = { durationMillis ->
                streamingVm.setSleepTimer(durationMillis)
            },
            onAlarmSet = { hour, minute, station ->
                streamingVm.setAlarm(hour, minute, station)
            },
            onAlarmCancel = { streamingVm.cancelAlarm() }
        )
    }
}

@Composable
private fun BottomActionBar(
    isPlaying: Boolean,
    isFmMode: Boolean,
    isPlayPauseEnabled: Boolean,
    onListClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onFmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .wrapContentWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(50)), // Fully rounded
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onListClick) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = "Station List",
                    tint = if (!isFmMode) Color.White else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            FilledIconButton(
                onClick = onPlayPauseClick,
                enabled = isPlayPauseEnabled,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color(0xFF3A3A3A)
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp),
                    tint = if (isPlayPauseEnabled) MaterialTheme.colorScheme.onPrimary else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            IconButton(onClick = onFmClick) {
                Icon(
                    Icons.Filled.Sensors,
                    contentDescription = "FM Radio",
                    tint = if (isFmMode) Color.White else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
