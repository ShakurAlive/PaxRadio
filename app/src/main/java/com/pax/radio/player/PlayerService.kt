package com.pax.radio.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.pax.radio.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : MediaSessionService() {

    @Inject lateinit var radioPlayer: RadioPlayer
    private lateinit var session: MediaSession

    override fun onCreate() {
        super.onCreate()
        createChannel()
        session = MediaSession.Builder(this, radioPlayer.exoPlayer)
            .setId("radio_session")
            .build()
        startForeground(1, buildNotification())
    }

    private fun buildNotification() = NotificationCompat.Builder(this, "radio_playback")
        .setContentTitle(getString(R.string.app_name))
        .setContentText("Streaming radio")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setOngoing(true)
        .build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel("radio_playback") == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        "radio_playback","Radio Playback", NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    override fun onDestroy() {
        session.release()
        super.onDestroy()
    }
}

