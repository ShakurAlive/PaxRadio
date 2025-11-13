package com.pax.radio.ui.fm

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.util.Range
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class FmRadioViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    private val range = Range(87.5f,108f)
    private val _freq = MutableStateFlow(99.9f)
    val freq = _freq.asStateFlow()

    private val _supported = MutableStateFlow(detectSupport())
    val supported = _supported.asStateFlow()

    private val _headphones = MutableStateFlow(isHeadphones())
    val headphones = _headphones.asStateFlow()

    private val _scanned = MutableStateFlow<List<Float>>(emptyList())
    val scanned = _scanned.asStateFlow()

    private fun detectSupport(): Boolean {
        val pm = ctx.packageManager
        return pm.hasSystemFeature("android.hardware.radio.fm") ||
               pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
    }

    private fun isHeadphones(): Boolean {
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        @Suppress("DEPRECATION") return am.isWiredHeadsetOn
    }

    fun refreshHeadphones() { _headphones.value = isHeadphones() }

    fun setFreq(f: Float) { _freq.value = f.coerceIn(range.lower, range.upper) }

    fun scan() {
        _scanned.value = List(6) {
            Random.nextDouble(87.5,108.0).toFloat().let { (Math.round(it*10)/10f) }
        }.sorted()
    }
}

