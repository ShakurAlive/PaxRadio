package com.pax.radio.ui

import android.content.Intent
import android.os.Build
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
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
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
import com.pax.radio.data.PlayerState
import com.pax.radio.player.RadioPlaybackService
import com.pax.radio.player.sound.SoundPlayer
import com.pax.radio.ui.components.*
import com.pax.radio.ui.fm.FmModeScreen
import com.pax.radio.ui.fm.FmRadioViewModel
import com.pax.radio.ui.navigation.NavRoute
import com.pax.radio.ui.settings.SettingsScreen
import com.pax.radio.ui.settings.SettingsViewModel
import com.pax.radio.ui.settings.ThemesScreen
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

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
    val themeName by settingsViewModel.theme.collectAsState()

    PaxRadioTheme(themeName = themeName) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = NavRoute.Streaming.route) {
            composable(NavRoute.Streaming.route) {
                MainRadioScreen(
                    soundPlayer = soundPlayer,
                    streamingVm = streamingVm,
                    onNavigateToSettings = { navController.navigate(NavRoute.Settings.route) }
                )
            }
            composable(NavRoute.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToThemes = { navController.navigate(NavRoute.Themes.route) },
                    onNavigateToGeneral = { navController.navigate(NavRoute.General.route) },
                    onNavigateToAbout = { navController.navigate(NavRoute.About.route) }
                )
            }
            composable(NavRoute.Themes.route) {
                ThemesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.General.route) {
                // TODO: Create GeneralSettingsScreen
            }
            composable(NavRoute.About.route) {
                com.pax.radio.ui.settings.AboutScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainRadioScreen(
    soundPlayer: SoundPlayer,
    streamingVm: StreamingViewModel,
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
        Scaffold(
            topBar = {
                RadioTopAppBar(
                    modifier = Modifier.padding(50.dp),
                    isPlaying = isPlaying,
                    sleepTimerActive = sleepTimerActive,
                    onSleepAlarmClick = { showSleepAlarm = true },
                    onSettingsClick = onNavigateToSettings,
                    isFmMode = fmMode,
                    onFmClick = {
                        fmMode = !fmMode
                        fmVm.refreshHeadphones()
                    }
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
                        isPlayPauseEnabled = current != null && current!!.isValidUrl,
                        onPrevClick = { streamingVm.selectPrevious() },
                        onNextClick = { streamingVm.selectNext() },
                        onShuffleClick = { streamingVm.selectRandom() },
                        prevEnabled = streamingVm.hasPrevious(),
                        nextEnabled = streamingVm.hasNext()
                    )
                }
            },
            containerColor = Color.Transparent
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
                        trackTitle = trackTitle,
                        playerState = playerState,
                        onToggleFavorite = {
                            current?.let { streamingVm.toggleFavorite(it.id) }
                        }
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
            onToggleGroup = { groupId ->
                streamingVm.toggleGroup(groupId)
            },
            onDismiss = { showStationSelector = false }
        )
    }

    if (showSleepAlarm) {
        val radioStations = stations.flatMap {
            when (it) {
                is com.pax.radio.data.RadioStation -> listOf(it)
                is com.pax.radio.data.RadioGroup -> it.stations
                else -> emptyList()
            }
        }
        SleepAlarmBottomSheet(
            currentStation = current,
            stations = radioStations,
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
    modifier: Modifier = Modifier,
    onPrevClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onShuffleClick: () -> Unit = {},
    prevEnabled: Boolean = true,
    nextEnabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .wrapContentWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(50)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station List button (слева)
            IconButton(onClick = onListClick, enabled = !isFmMode) {
                Icon(
                    painter = painterResource(R.drawable.menu_open_icon),
                    contentDescription = "Station List",
                    tint = if (!isFmMode)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(42.dp)
                        .padding(1.dp) // иногда помогает подогнать под размер Material-иконок
                )
            }

            // Центрированная группа: Prev, Play/Pause, Next
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevClick, enabled = prevEnabled && !isFmMode) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = if (prevEnabled && !isFmMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onPlayPauseClick, enabled = isPlayPauseEnabled) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = "Play/Pause",
                        tint = if (isPlayPauseEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onNextClick, enabled = nextEnabled && !isFmMode) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = if (nextEnabled && !isFmMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            // Shuffle button (справа)
            IconButton(onClick = onShuffleClick, enabled = !isFmMode) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (!isFmMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}
