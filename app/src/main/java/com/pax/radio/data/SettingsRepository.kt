package com.pax.radio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
    }

    val selectedTheme: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_THEME] ?: AppTheme.NEON.name
        }

    suspend fun setSelectedTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_THEME] = themeName
        }
    }
}

enum class AppTheme {
    NEON,
    BORDEAUX
}
