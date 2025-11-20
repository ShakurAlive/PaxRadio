package com.pax.radio.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import coil.imageLoader
import coil.request.ImageRequest
import com.pax.radio.R
import com.pax.radio.data.RadioStation
import com.pax.radio.data.RadioStationParser
import com.pax.radio.receiver.AlarmReceiver
import com.pax.radio.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RadioPlaybackService : MediaSessionService() {

    @Inject
    lateinit var radioPlayer: RadioPlayer

    private lateinit var mediaSession: MediaSession
    private val CHANNEL_ID = "radio_playback_channel"
    private val NOTIFICATION_ID = 1

    private var currentStation: RadioStation? = null
    private var stations: List<RadioStation> = emptyList()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        stations = RadioStationParser.parseFromAssets(this).flatMap {
            when (it) {
                is RadioStation -> listOf(it)
                is com.pax.radio.data.RadioGroup -> it.stations
                else -> emptyList()
            }
        }

        mediaSession = MediaSession.Builder(this, radioPlayer.exoPlayer)
            .setId("PaxRadioSession")
            .build()

        radioPlayer.exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Radio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows currently playing radio station"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    fun updateStation(station: RadioStation?) {
        currentStation = station
        updateNotification()
    }

    private fun updateNotification() {
        serviceScope.launch {
            val stationLogo = loadBitmap(currentStation?.imageUrl)
            val notification = buildNotification(stationLogo)
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private suspend fun loadBitmap(url: String?): Bitmap? {
        if (url.isNullOrEmpty()) return null
        val request = ImageRequest.Builder(this)
            .data(url)
            .allowHardware(false) // Disable hardware bitmaps for notifications
            .build()
        val drawable = imageLoader.execute(request).drawable
        return (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
    }

    @Suppress("UnsafeOptInUsageError")
    private fun buildNotification(stationLogo: Bitmap?): Notification {
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (radioPlayer.isPlaying()) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "Pause",
                createActionIntent(ACTION_PLAY_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "Play",
                createActionIntent(ACTION_PLAY_PAUSE)
            )
        }

        val prevAction = NotificationCompat.Action(
            R.drawable.ic_previous,
            "Previous",
            createActionIntent(ACTION_PREVIOUS)
        )

        val nextAction = NotificationCompat.Action(
            R.drawable.ic_next,
            "Next",
            createActionIntent(ACTION_NEXT)
        )

        val shuffleAction = NotificationCompat.Action(
            R.drawable.ic_shuffle,
            "Shuffle",
            createActionIntent(ACTION_SHUFFLE)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentStation?.name ?: "PaxRadio")
            .setContentText(if (radioPlayer.isPlaying()) "Playing" else "Paused")
            .setSmallIcon(R.drawable.ic_radio)
            .setLargeIcon(stationLogo)
            .setContentIntent(contentIntent)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(shuffleAction)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(radioPlayer.isPlaying())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(this, RadioPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun playStation(station: RadioStation) {
        currentStation = station
        radioPlayer.play(station.id, station.streamUrl)
        updateNotification()
        notifyPlaybackStateChanged(station, true)
    }

    private fun notifyPlaybackStateChanged(station: RadioStation, isPlaying: Boolean) {
        val broadcastIntent = Intent(ACTION_PLAYBACK_STATE_CHANGED).apply {
            setPackage(packageName)
            putExtra(EXTRA_STATION_ID, station.id)
            putExtra(EXTRA_STATION_URL, station.streamUrl)
            putExtra("station_name", station.name)
            putExtra("is_playing", isPlaying)
        }
        sendBroadcast(broadcastIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("RadioPlaybackService", "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                android.util.Log.d("RadioPlaybackService", "ACTION_PLAY_PAUSE received")
                if (radioPlayer.isPlaying()) {
                    radioPlayer.pause()
                } else {
                    radioPlayer.resume()
                }
                updateNotification()
            }
            ACTION_PREVIOUS -> {
                val currentIndex = stations.indexOf(currentStation)
                if (currentIndex > 0) {
                    playStation(stations[currentIndex - 1])
                }
            }
            ACTION_NEXT -> {
                val currentIndex = stations.indexOf(currentStation)
                if (currentIndex != -1 && currentIndex < stations.size - 1) {
                    playStation(stations[currentIndex + 1])
                }
            }
            ACTION_SHUFFLE -> {
                if (stations.isNotEmpty()) {
                    playStation(stations.random())
                }
            }
            ACTION_PLAY_ALARM -> {
                android.util.Log.d("RadioPlaybackService", "ACTION_PLAY_ALARM received")
                val stationId = intent.getStringExtra(AlarmReceiver.EXTRA_STATION_ID) ?: ""
                val stationUrl = intent.getStringExtra(AlarmReceiver.EXTRA_STATION_URL) ?: ""
                val stationName = intent.getStringExtra("station_name") ?: ""
                android.util.Log.d("RadioPlaybackService", "Station ID: $stationId, URL: $stationUrl")
                if (stationUrl.isNotEmpty()) {
                    android.util.Log.d("RadioPlaybackService", "Starting playback...")

                    // Request audio focus
                    val audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
                    audioManager.requestAudioFocus(
                        null,
                        android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.AUDIOFOCUS_GAIN
                    )

                    // Set volume
                    radioPlayer.setVolume(1.0f)

                    // Start playback
                    val station = RadioStation(stationId, stationName, stationUrl, "")
                    playStation(station)

                } else {
                    android.util.Log.e("RadioPlaybackService", "Station URL is empty!")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaSession.release()
        radioPlayer.stop()
        serviceJob.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.paxradio.ACTION_PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.example.paxradio.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.example.paxradio.ACTION_NEXT"
        const val ACTION_SHUFFLE = "com.example.paxradio.ACTION_SHUFFLE"
        const val ACTION_UPDATE_STATION = "com.example.paxradio.ACTION_UPDATE_STATION"
        const val ACTION_PLAY_ALARM = "com.example.paxradio.ACTION_PLAY_ALARM"
        const val ACTION_PLAYBACK_STATE_CHANGED = "com.example.paxradio.ACTION_PLAYBACK_STATE_CHANGED"
        const val EXTRA_STATION = "extra_station"
        const val EXTRA_STATION_ID = "station_id"
        const val EXTRA_STATION_URL = "station_url"
    }
}
