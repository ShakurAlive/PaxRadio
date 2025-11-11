package com.example.paxradio.player

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioPlayer @Inject constructor(
    val exoPlayer: ExoPlayer
) {
    private var currentId: String? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("RadioPlayer", "Playback error: ${error.message}")
            }
        })
    }

    fun play(id: String, url: String): Boolean {
        return try {
            if (url.isBlank()) {
                Log.e("RadioPlayer", "Cannot play: empty URL")
                return false
            }

            if (currentId == id && exoPlayer.isPlaying) {
                return true
            }

            stop()
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
            exoPlayer.play()
            currentId = id
            true
        } catch (e: Exception) {
            Log.e("RadioPlayer", "Error playing stream: ${e.message}")
            false
        }
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    fun pause() = exoPlayer.pause()

    fun resume() {
        if (exoPlayer.playbackState == Player.STATE_READY) {
            exoPlayer.play()
        }
    }

    fun isPlaying(): Boolean = exoPlayer.isPlaying
    fun volume(): Float = exoPlayer.volume
    fun setVolume(v: Float) { exoPlayer.volume = v.coerceIn(0f,1f) }
    fun release() = exoPlayer.release()
}