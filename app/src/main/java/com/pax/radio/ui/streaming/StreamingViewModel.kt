package com.pax.radio.ui.streaming

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pax.radio.data.DisplayableItem
import com.pax.radio.data.PlayerState
import com.pax.radio.data.RadioGroup
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

    private val _stations = MutableStateFlow<List<DisplayableItem>>(emptyList())
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

    private val sharedPreferences = context.getSharedPreferences("radio_prefs", Context.MODE_PRIVATE)

    init {
        loadStations()
        observePlayerState()
        observeFavorites()
        observeSleepTimer()

        val lastPlayedStationId = sharedPreferences.getString("last_played_station_id", null)
        if (lastPlayedStationId != null) {
            viewModelScope.launch {
                val station = findStationById(lastPlayedStationId)
                if (station != null) {
                    _current.value = station
                }
            }
        }
    }

    private fun observeSleepTimer() {
        viewModelScope.launch {
            sleepTimerManager.remainingMillis.collect { millis ->
                _sleepTimerMinutes.value = (millis / 60000).toInt()
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect { favoriteIds ->
                val updatedStations = _stations.value.map { item ->
                    when (item) {
                        is RadioStation -> item.copy(isFavorite = favoriteIds.contains(item.id))
                        is RadioGroup -> {
                            val updatedGroupStations = item.stations.map { station ->
                                station.copy(isFavorite = favoriteIds.contains(station.id))
                            }
                            item.copy(stations = updatedGroupStations)
                        }
                        else -> item
                    }
                }
                _stations.value = updatedStations.sortedByDescending {
                    when (it) {
                        is RadioStation -> it.isFavorite
                        is RadioGroup -> it.stations.any { s -> s.isFavorite }
                        else -> false
                    }
                }

                _current.value?.let { currentStation ->
                    val updatedCurrent = findStationById(currentStation.id)
                    if (updatedCurrent != null) {
                        _current.value = updatedCurrent
                    }
                }
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

    private fun findStationById(stationId: String): RadioStation? {
        return flattenedStations().find { it.id == stationId }
    }

    private fun flattenedStations(): List<RadioStation> {
        return _stations.value.flatMap {
            when (it) {
                is RadioStation -> listOf(it)
                is RadioGroup -> it.stations
                else -> emptyList()
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying()) {
                    if (_playerState.value !is PlayerState.Playing) {
                        _playerState.value = PlayerState.Playing
                    }
                } else {
                    if (_playerState.value is PlayerState.Playing) {
                        _playerState.value = PlayerState.Paused
                    }
                }
                kotlinx.coroutines.delay(1000)
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
            sharedPreferences.edit { putString("last_played_station_id", station.id) }
        } else {
            _playerState.value = PlayerState.Error
            viewModelScope.launch {
                Toast.makeText(context, "Failed to play ${station.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggle() {
        when (_playerState.value) {
            is PlayerState.Playing, is PlayerState.Buffering -> {
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
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM
            putExtra(AlarmReceiver.EXTRA_STATION_ID, station.id)
            putExtra(AlarmReceiver.EXTRA_STATION_NAME, station.name)
            putExtra(AlarmReceiver.EXTRA_STATION_URL, station.streamUrl)
        }

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

    fun toggleGroup(groupId: String) {
        val updatedStations = _stations.value.map {
            if (it is RadioGroup && it.id == groupId) {
                it.copy(isExpanded = !it.isExpanded)
            } else {
                it
            }
        }
        _stations.value = updatedStations
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(stationId)
        }
    }

    fun hasNext(): Boolean = _current.value != null && flattenedStations().size > 1
    fun hasPrevious(): Boolean = hasNext()

    fun selectNext() {
        val stations = flattenedStations()
        val currentStation = _current.value ?: return
        if (stations.isEmpty()) return
        val idx = stations.indexOfFirst { it.id == currentStation.id }
        if (idx == -1) return
        val nextIdx = (idx + 1) % stations.size
        select(stations[nextIdx])
    }

    fun selectPrevious() {
        val stations = flattenedStations()
        val currentStation = _current.value ?: return
        if (stations.isEmpty()) return
        val idx = stations.indexOfFirst { it.id == currentStation.id }
        if (idx == -1) return
        val prevIdx = (idx - 1 + stations.size) % stations.size
        select(stations[prevIdx])
    }

    fun selectRandom() {
        val stations = flattenedStations()
        if (stations.isEmpty()) return
        val validStations = stations.filter { it.isValidUrl }
        if (validStations.isEmpty()) return

        val currentId = _current.value?.id
        val availableStations = if (validStations.size > 1 && currentId != null) {
            validStations.filter { it.id != currentId }
        } else {
            validStations
        }

        val randomStation = availableStations.random()
        select(randomStation)
    }

    fun syncPlayerState(stationId: String, stationUrl: String, stationName: String) {
        viewModelScope.launch {
            val station = findStationById(stationId) ?: RadioStation(
                id = stationId,
                name = stationName,
                streamUrl = stationUrl,
                description = "Internet Radio",
                imageUrl = null
            )
            _current.value = station
            _playerState.value = PlayerState.Playing
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.stop()
        sleepTimerManager.cancelTimer()
    }
}
