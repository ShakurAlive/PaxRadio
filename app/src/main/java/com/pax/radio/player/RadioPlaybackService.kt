package com.pax.radio.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.widget.RemoteViews
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

    @Inject
    lateinit var settingsRepository: com.pax.radio.data.SettingsRepository

    private lateinit var mediaSession: MediaSession
    private val CHANNEL_ID = "radio_playback_channel"
    private val NOTIFICATION_ID = 1

    private var currentStation: RadioStation? = null
    private var currentTheme: com.pax.radio.data.Theme? = null
    private var stations: List<RadioStation> = emptyList()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        serviceScope.launch {
            stations = RadioStationParser.parseFromAssets(this@RadioPlaybackService).flatMap {
                when (it) {
                    is RadioStation -> listOf(it)
                    is com.pax.radio.data.RadioGroup -> it.stations
                    else -> emptyList()
                }
            }

            val themes = com.pax.radio.data.ThemeParser.parse(this@RadioPlaybackService)
            settingsRepository.selectedTheme.collect { themeName ->
                currentTheme = themes.find { it.name == themeName } ?: themes.first()
                updateNotification()
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

        // Get the layout for the custom notification
        val collapsedView = RemoteViews(packageName, R.layout.notification_collapsed)
        val expandedView = RemoteViews(packageName, R.layout.notification_expanded)

        // Apply data and colors
        val title = currentStation?.name ?: "PaxRadio"
        val text = radioPlayer.getCurrentTrackTitle() ?: if (radioPlayer.isPlaying()) "Playing" else "Paused"
        val playPauseIcon = if (radioPlayer.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play

        // Collapsed View
        collapsedView.setTextViewText(R.id.notification_title, title)
        collapsedView.setTextViewText(R.id.notification_text, text)
        collapsedView.setImageViewBitmap(R.id.notification_logo, stationLogo ?: drawableToBitmap(R.drawable.ic_radio))
        collapsedView.setImageViewResource(R.id.notification_play_pause, playPauseIcon)

        // Expanded View
        expandedView.setTextViewText(R.id.notification_title, title)
        expandedView.setTextViewText(R.id.notification_text, text)
        expandedView.setImageViewBitmap(R.id.notification_logo, stationLogo ?: drawableToBitmap(R.drawable.ic_radio))
        expandedView.setImageViewResource(R.id.notification_play_pause, playPauseIcon)

        // Apply colors from theme
        currentTheme?.let {
            val bgColor = android.graphics.Color.parseColor(it.backgroundColor)
            val primaryColor = android.graphics.Color.parseColor(it.primaryTextColor)
            val secondaryColor = android.graphics.Color.parseColor(it.secondaryTextColor)

            collapsedView.setInt(R.id.notification_root, "setBackgroundColor", bgColor)
            collapsedView.setTextColor(R.id.notification_title, primaryColor)
            collapsedView.setTextColor(R.id.notification_text, secondaryColor)
            collapsedView.setInt(R.id.notification_play_pause, "setColorFilter", primaryColor)

            expandedView.setInt(R.id.notification_root, "setBackgroundColor", bgColor)
            expandedView.setTextColor(R.id.notification_title, primaryColor)
            expandedView.setTextColor(R.id.notification_text, secondaryColor)
            expandedView.setInt(R.id.notification_prev, "setColorFilter", primaryColor)
            expandedView.setInt(R.id.notification_play_pause, "setColorFilter", primaryColor)
            expandedView.setInt(R.id.notification_next, "setColorFilter", primaryColor)
        }

        // Set pending intents for buttons
        collapsedView.setOnClickPendingIntent(R.id.notification_play_pause, createActionIntent(ACTION_PLAY_PAUSE))
        expandedView.setOnClickPendingIntent(R.id.notification_prev, createActionIntent(ACTION_PREVIOUS))
        expandedView.setOnClickPendingIntent(R.id.notification_play_pause, createActionIntent(ACTION_PLAY_PAUSE))
        expandedView.setOnClickPendingIntent(R.id.notification_next, createActionIntent(ACTION_NEXT))


        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_radio)
            .setContentIntent(contentIntent)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(radioPlayer.isPlaying())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun drawableToBitmap(drawableId: Int): Bitmap {
        val drawable = getDrawable(drawableId)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
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
