package com.pax.radio.ui.streaming

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pax.radio.data.PlayerState
import com.pax.radio.data.RadioStation
import com.pax.radio.data.RadioStationParser
import com.pax.radio.player.RadioPlayer
import com.pax.radio.player.SleepTimerManager
import com.pax.radio.receiver.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StreamingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: RadioPlayer,
    private val sleepTimerManager: SleepTimerManager,
    private val favoritesRepository: com.pax.radio.data.FavoritesRepository
) : ViewModel() {

    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    val stationsFlow = _stations.asStateFlow()

    private val _current = MutableStateFlow<RadioStation?>(null)
    val current = _current.asStateFlow()

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState = _playerState.asStateFlow()

    val trackTitle = player.trackTitle

    private val _volume = MutableStateFlow(0.5f)
    val volume = _volume.asStateFlow()

    val sleepTimerActive = sleepTimerManager.isActive
    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes = _sleepTimerMinutes.asStateFlow()
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        loadStations()
        observeSleepTimer()
        observePlayerState()
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect { favoriteIds ->
                // Обновляем список станций с учетом избранного
                val updatedStations = _stations.value.map { station ->
                    station.copy(isFavorite = favoriteIds.contains(station.id))
                }
                // Сортируем: избранные вначале
                _stations.value = updatedStations.sortedByDescending { it.isFavorite }

                // ВАЖНО: Обновляем текущую станцию если она есть
                _current.value?.let { currentStation ->
                    val updatedCurrent = updatedStations.find { it.id == currentStation.id }
                    if (updatedCurrent != null) {
                        _current.value = updatedCurrent
                    }
                }
            }
        }
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(stationId)
        }
    }

    // === Navigation between stations ===
    fun hasNext(): Boolean = _current.value != null && _stations.value.size > 1
    fun hasPrevious(): Boolean = hasNext()

    fun selectNext() {
        val stations = _stations.value
        val currentStation = _current.value ?: return
        if (stations.isEmpty()) return
        val idx = stations.indexOfFirst { it.id == currentStation.id }
        if (idx == -1) return
        val nextIdx = (idx + 1) % stations.size // wrap-around
        select(stations[nextIdx])
    }

    fun selectPrevious() {
        val stations = _stations.value
        val currentStation = _current.value ?: return
        if (stations.isEmpty()) return
        val idx = stations.indexOfFirst { it.id == currentStation.id }
        if (idx == -1) return
        val prevIdx = (idx - 1 + stations.size) % stations.size // wrap-around
        select(stations[prevIdx])
    }

    fun selectRandom() {
        val stations = _stations.value
        if (stations.isEmpty()) return
        val validStations = stations.filter { it.isValidUrl }
        if (validStations.isEmpty()) return

        // Выбираем случайную станцию, отличную от текущей
        val currentId = _current.value?.id
        val availableStations = if (validStations.size > 1 && currentId != null) {
            validStations.filter { it.id != currentId }
        } else {
            validStations
        }

        val randomStation = availableStations.random()
        select(randomStation)
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            // Периодически проверяем состояние плеера
            kotlinx.coroutines.delay(1000)
            while (true) {
                if (player.isPlaying()) {
                    if (_playerState.value !is PlayerState.Playing) {
                        android.util.Log.d("StreamingViewModel", "Player is playing, updating state")
                        _playerState.value = PlayerState.Playing
                    }

                    // Если current == null, но плеер играет, нужно найти станцию
                    if (_current.value == null && player.exoPlayer.currentMediaItem != null) {
                        val mediaId = player.exoPlayer.currentMediaItem?.mediaId
                        android.util.Log.d("StreamingViewModel", "Found playing media ID: $mediaId")
                        if (mediaId != null) {
                            val station = _stations.value.find { it.id == mediaId }
                            if (station != null) {
                                android.util.Log.d("StreamingViewModel", "Auto-detected station: ${station.name}")
                                _current.value = station
                            }
                        }
                    }
                } else if (!player.isPlaying() && _playerState.value is PlayerState.Playing) {
                    _playerState.value = PlayerState.Paused
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun syncPlayerState(stationId: String, stationUrl: String, stationName: String) {
        android.util.Log.d("StreamingViewModel", "syncPlayerState called")
        val station = _stations.value.find { it.id == stationId }
            ?: RadioStation(stationId, stationName, stationUrl, "")
        _current.value = station
        _playerState.value = PlayerState.Playing
        android.util.Log.d("StreamingViewModel", "State updated: current=${station.name}, playing=true")
    }

    private fun observeSleepTimer() {
        viewModelScope.launch {
            sleepTimerManager.remainingMillis.collect { millis ->
                _sleepTimerMinutes.value = (millis / 60000).toInt()
            }
        }
    }

    private fun loadStations() {
        viewModelScope.launch {
            _stations.value = withContext(Dispatchers.IO) {
                RadioStationParser.parseFromAssets(context)
            }
        }
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
        }
    }
    
    fun setSleepTimer(durationMillis: Long) {
        if (durationMillis > 0) {
            sleepTimerManager.startTimer(durationMillis) {
                player.stop()
                _current.value = null
                _playerState.value = PlayerState.Idle
            }
        } else {
            sleepTimerManager.cancelTimer()
        }
    }

    fun setAlarm(hour: Int, minute: Int, station: RadioStation) {
        android.util.Log.d("StreamingViewModel", "setAlarm called: $hour:$minute, station: ${station.name}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM
            putExtra(AlarmReceiver.EXTRA_STATION_ID, station.id)
            putExtra(AlarmReceiver.EXTRA_STATION_NAME, station.name)
            putExtra(AlarmReceiver.EXTRA_STATION_URL, station.streamUrl)
        }
        android.util.Log.d("StreamingViewModel", "Intent created with action: ${AlarmReceiver.ACTION_ALARM}")
        android.util.Log.d("StreamingViewModel", "Station ID: ${station.id}, URL: ${station.streamUrl}")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        android.util.Log.d("StreamingViewModel", "Alarm time: ${calendar.time}")
        android.util.Log.d("StreamingViewModel", "Current time: ${Calendar.getInstance().time}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        viewModelScope.launch {
            Toast.makeText(context, "Alarm set for ${String.format("%02d:%02d", hour, minute)}", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        viewModelScope.launch {
            Toast.makeText(context, "Alarm canceled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.stop()
        sleepTimerManager.cancelTimer()
    }
}
