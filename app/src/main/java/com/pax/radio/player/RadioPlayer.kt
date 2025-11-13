package com.pax.radio.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioPlayer @Inject constructor(
    @ApplicationContext context: Context
) {
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _trackTitle = MutableStateFlow<String?>(null)
    val trackTitle = _trackTitle.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                _trackTitle.value = mediaMetadata.title?.toString()
            }
        })
    }

    fun play(stationId: String, url: String): Boolean {
        return try {
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMediaId(stationId)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun resume() = exoPlayer.play()
    fun pause() = exoPlayer.pause()
    fun stop() = exoPlayer.stop()
    fun isPlaying() = exoPlayer.isPlaying
    fun setVolume(volume: Float) {
        exoPlayer.volume = volume
    }
}
