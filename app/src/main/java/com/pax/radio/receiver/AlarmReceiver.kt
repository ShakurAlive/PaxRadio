package com.pax.radio.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.pax.radio.player.RadioPlaybackService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "onReceive called with action: ${intent.action}")
        when (intent.action) {
            ACTION_ALARM -> {
                val stationId = intent.getStringExtra(EXTRA_STATION_ID)
                val stationName = intent.getStringExtra(EXTRA_STATION_NAME)
                val stationUrl = intent.getStringExtra(EXTRA_STATION_URL)

                Log.d("AlarmReceiver", "Alarm triggered!")
                Log.d("AlarmReceiver", "Station ID: $stationId")
                Log.d("AlarmReceiver", "Station Name: $stationName")
                Log.d("AlarmReceiver", "Station URL: $stationUrl")

                // Start playback service
                val serviceIntent = Intent(context, RadioPlaybackService::class.java).apply {
                    action = RadioPlaybackService.ACTION_PLAY_ALARM
                    putExtra(RadioPlaybackService.EXTRA_STATION_ID, stationId)
                    putExtra(RadioPlaybackService.EXTRA_STATION_URL, stationUrl)
                    putExtra("station_name", stationName)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("AlarmReceiver", "Device booted - rescheduling alarms")
                // Reschedule alarms from SharedPreferences
                rescheduleAlarms(context)
            }
        }
    }

    private fun rescheduleAlarms(context: Context) {
        val prefs = context.getSharedPreferences("paxradio_prefs", Context.MODE_PRIVATE)
        val hasAlarm = prefs.getBoolean("alarm_enabled", false)

        if (hasAlarm) {
            val hour = prefs.getInt("alarm_hour", 7)
            val minute = prefs.getInt("alarm_minute", 30)
            val stationId = prefs.getString("alarm_station_id", "")
            val stationName = prefs.getString("alarm_station_name", "")
            val stationUrl = prefs.getString("alarm_station_url", "")

            if (stationId != null && stationUrl != null) {
                // Reschedule alarm
                Log.d("AlarmReceiver", "Rescheduling alarm for $hour:$minute")
            }
        }
    }

    companion object {
        const val ACTION_ALARM = "com.example.paxradio.ACTION_ALARM"
        const val EXTRA_STATION_ID = "station_id"
        const val EXTRA_STATION_NAME = "station_name"
        const val EXTRA_STATION_URL = "station_url"
    }
}

