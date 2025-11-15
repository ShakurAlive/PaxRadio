package com.pax.radio.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.favoritesDataStore by preferencesDataStore(name = "favorites")

@Singleton
class FavoritesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val FAVORITES_KEY = stringSetPreferencesKey("favorite_stations")

    val favoriteIds: Flow<Set<String>> = context.favoritesDataStore.data
        .map { preferences ->
            preferences[FAVORITES_KEY] ?: emptySet()
        }

    suspend fun toggleFavorite(stationId: String) {
        context.favoritesDataStore.edit { preferences ->
            val currentFavorites: MutableSet<String> = (preferences[FAVORITES_KEY] ?: emptySet()).toMutableSet()
            if (currentFavorites.contains(stationId)) {
                currentFavorites.remove(stationId)
            } else {
                currentFavorites.add(stationId)
            }
            preferences[FAVORITES_KEY] = currentFavorites
        }
    }
}

