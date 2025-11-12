package com.example.paxradio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paxradio.data.AppTheme
import com.example.paxradio.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val theme: StateFlow<AppTheme> = settingsRepository.selectedTheme
        .map { AppTheme.valueOf(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.NEON
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setSelectedTheme(theme.name)
        }
    }
}
