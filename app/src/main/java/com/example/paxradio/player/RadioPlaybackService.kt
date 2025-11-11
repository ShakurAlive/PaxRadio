package com.example.paxradio.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.paxradio.R
import com.example.paxradio.data.RadioStation
import com.example.paxradio.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RadioPlaybackService : MediaSessionService() {

    @Inject
    lateinit var radioPlayer: RadioPlayer

    private lateinit var mediaSession: MediaSession
    private val CHANNEL_ID = "radio_playback_channel"
    private val NOTIFICATION_ID = 1

    private var currentStation: RadioStation? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

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
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    @Suppress("UnsafeOptInUsageError")
    private fun buildNotification(): Notification {
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
                createPlayPauseIntent()
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "Play",
                createPlayPauseIntent()
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentStation?.name ?: "PaxRadio")
            .setContentText(if (radioPlayer.isPlaying()) "Playing" else "Paused")
            .setSmallIcon(R.drawable.ic_radio)
            .setContentIntent(contentIntent)
            .addAction(playPauseAction)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(0)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(radioPlayer.isPlaying())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createPlayPauseIntent(): PendingIntent {
        val intent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                if (radioPlayer.isPlaying()) {
                    radioPlayer.pause()
                } else {
                    radioPlayer.resume()
                }
                updateNotification()
            }
            ACTION_PLAY_ALARM -> {
                val stationId = intent.getStringExtra("station_id") ?: ""
                val stationUrl = intent.getStringExtra("station_url") ?: ""
                if (stationUrl.isNotEmpty()) {
                    radioPlayer.play(stationId, stationUrl)
                    updateNotification()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaSession.release()
        radioPlayer.stop()
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.paxradio.ACTION_PLAY_PAUSE"
        const val ACTION_UPDATE_STATION = "com.example.paxradio.ACTION_UPDATE_STATION"
        const val ACTION_PLAY_ALARM = "com.example.paxradio.ACTION_PLAY_ALARM"
        const val EXTRA_STATION = "extra_station"
    }
}

