package com.example.paxradio.ui.streaming

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paxradio.data.PlayerState
import com.example.paxradio.data.RadioStation
import com.example.paxradio.data.RadioStationParser
import com.example.paxradio.player.RadioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreamingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: RadioPlayer,
    private val sleepTimerManager: com.example.paxradio.player.SleepTimerManager
) : ViewModel() {

    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    val stationsFlow = _stations.asStateFlow()

    private val _current = MutableStateFlow<RadioStation?>(null)
    val current = _current.asStateFlow()

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState = _playerState.asStateFlow()

    private val _volume = MutableStateFlow(0.5f) // Start with a default volume
    val volume = _volume.asStateFlow()

    val sleepTimerActive = sleepTimerManager.isActive
    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes = _sleepTimerMinutes.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        loadStations()
        observeSleepTimer()
    }

    private fun observeSleepTimer() {
        viewModelScope.launch {
            sleepTimerManager.remainingMillis.collect { millis ->
                _sleepTimerMinutes.value = (millis / 60000).toInt()
            }
        }
    }

    private fun loadStations() {
        _stations.value = RadioStationParser.parseFromAssets(context)
    }

    fun select(station: RadioStation) {
        if (!station.isValidUrl) {
            _playerState.value = PlayerState.NoStream
            viewModelScope.launch {
                Toast.makeText(context, "Stream not available for ${station.name}", Toast.LENGTH_SHORT).show()
            }
            return
        }

        player.stop()
        val success = player.play(station.id, station.streamUrl)

        if (success) {
            _current.value = station
            _playerState.value = PlayerState.Playing
        } else {
            _playerState.value = PlayerState.Error
            viewModelScope.launch {
                Toast.makeText(context, "Failed to play ${station.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggle() {
        when (_playerState.value) {
            is PlayerState.Playing -> {
                player.pause()
                _playerState.value = PlayerState.Paused
            }
            is PlayerState.Paused, is PlayerState.Idle -> {
                val station = _current.value
                if (station != null) {
                    if (station.isValidUrl) {
                        player.resume()
                        _playerState.value = PlayerState.Playing
                    } else {
                        _playerState.value = PlayerState.NoStream
                        viewModelScope.launch {
                            Toast.makeText(context, "No stream URL available", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    viewModelScope.launch {
                        Toast.makeText(context, "Please select a station first", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            is PlayerState.NoStream -> {
                viewModelScope.launch {
                    Toast.makeText(context, "No stream URL available", Toast.LENGTH_SHORT).show()
                }
            }
            is PlayerState.Error -> {
                _current.value?.let { select(it) }
            }
        }
    }

    fun setVolume(v: Float) {
        val newVolume = v.coerceIn(0f, 1f)
        if (_volume.value != newVolume) {
            _volume.value = newVolume
            player.setVolume(newVolume)

            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val systemVolume = (newVolume * maxVolume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemVolume, 0)
        }
    }

    fun setSleepTimer(durationMillis: Long) {
        if (durationMillis > 0) {
            sleepTimerManager.startTimer(durationMillis) {
                player.stop()
                _playerState.value = PlayerState.Idle
            }
        } else {
            sleepTimerManager.cancelTimer()
        }
    }

    fun setAlarm(hour: Int, minute: Int, station: RadioStation) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.paxradio.receiver.AlarmReceiver::class.java).apply {
            action = com.example.paxradio.receiver.AlarmReceiver.ACTION_ALARM
            putExtra(com.example.paxradio.receiver.AlarmReceiver.EXTRA_STATION_ID, station.id)
            putExtra(com.example.paxradio.receiver.AlarmReceiver.EXTRA_STATION_NAME, station.name)
            putExtra(com.example.paxradio.receiver.AlarmReceiver.EXTRA_STATION_URL, station.streamUrl)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DATE, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        viewModelScope.launch {
            Toast.makeText(context, "Alarm set for ${String.format("%02d:%02d", hour, minute)}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.stop()
        sleepTimerManager.cancelTimer()
    }
}
