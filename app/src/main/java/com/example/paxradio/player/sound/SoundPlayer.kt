package com.example.paxradio.player.sound

import android.content.Context
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = -1
    private var isLoaded = false

    init {
        // Disable sound loading for now as it causes issues
        // Sound effects are not critical for app functionality
        android.util.Log.d("SoundPlayer", "SoundPlayer initialized (sound loading disabled)")
    }

    fun playClick() {
        // Disabled - sound effects not critical
        // No-op for now
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
