package com.pax.radio.player

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepTimerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var countDownTimer: CountDownTimer? = null
    private val _remainingMillis = MutableStateFlow(0L)
    val remainingMillis = _remainingMillis.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    fun startTimer(durationMillis: Long, onFinish: () -> Unit) {
        cancelTimer()

        if (durationMillis == 0L) {
            return
        }

        _isActive.value = true
        _remainingMillis.value = durationMillis

        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingMillis.value = millisUntilFinished
            }

            override fun onFinish() {
                _isActive.value = false
                _remainingMillis.value = 0L
                onFinish()
                Log.d("SleepTimer", "Sleep timer finished - stopping playback")
            }
        }.start()

        Log.d("SleepTimer", "Sleep timer started for ${durationMillis / 60000} minutes")
    }

    fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        _isActive.value = false
        _remainingMillis.value = 0L
        Log.d("SleepTimer", "Sleep timer cancelled")
    }

    fun getRemainingMinutes(): Int {
        return (_remainingMillis.value / 60000).toInt()
    }

    fun getRemainingSeconds(): Int {
        return ((_remainingMillis.value % 60000) / 1000).toInt()
    }
}
