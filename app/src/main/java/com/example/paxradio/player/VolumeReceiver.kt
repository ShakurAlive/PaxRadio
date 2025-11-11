package com.example.paxradio.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.paxradio.ui.streaming.StreamingViewModel

class VolumeReceiver(private val viewModel: StreamingViewModel) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
            val volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0)
            val maxVolume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_MAX_VOLUME", 100)
            viewModel.setVolume(volume.toFloat() / maxVolume.toFloat())
        }
    }
}
