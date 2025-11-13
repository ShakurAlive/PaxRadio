package com.pax.radio.data

import android.content.Context
import java.io.BufferedReader

object RadioStationParser {
    fun parseFromAssets(context: Context): List<RadioStation> {
        return try {
            val inputStream = context.assets.open("radio_assets/radio.list")
            val reader = BufferedReader(inputStream.reader())

            reader.lineSequence()
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .mapIndexed { index, line ->
                    val parts = line.split("|")
                    if (parts.size >= 2) {
                        RadioStation(
                            id = "station_$index",
                            name = parts[0].trim(),
                            streamUrl = parts[1].trim(),
                            description = "Internet Radio",
                            imageUrl = if (parts.size > 2) "radio_assets/logos/${parts[2].trim()}" else null
                        )
                    } else null
                }
                .filterNotNull()
                .toList()
        } catch (e: Exception) {
            // Return default stations if file not found
            listOf(
                RadioStation("1", "Rock FM", "https://example.com/rock.mp3", "Rock classics"),
                RadioStation("2", "Jazz Radio", "https://example.com/jazz.mp3", "Smooth jazz"),
                RadioStation("3", "News 24", "https://example.com/news.mp3", "Global news"),
                RadioStation("4", "Pop Hits", "https://example.com/pop.mp3", "Top charts"),
                RadioStation("5", "LoFi Beats", "https://example.com/lofi.mp3", "Chill study")
            )
        }
    }
}
