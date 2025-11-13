package com.pax.radio.player.sound

import android.content.Context
import android.media.SoundPool
import com.pax.radio.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool = SoundPool.Builder().setMaxStreams(1).build()
    private val clickSoundId = soundPool.load(context, R.raw.click, 1)

    fun playClick() {
        soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f)
    }
}
