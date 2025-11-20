package com.pax.radio.data

import android.content.Context
import java.io.BufferedReader
import java.util.regex.Pattern

object RadioStationParser {
    fun parseFromAssets(context: Context): List<DisplayableItem> {
        return try {
            val inputStream = context.assets.open("radio_assets/radio.list")
            val reader = BufferedReader(inputStream.reader())

            val stations = reader.lineSequence()
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .mapIndexed { index, line ->
                    val parts = line.split("|")
                    if (parts.size >= 2) {
                        RadioStation(
                            id = "station_$index",
                            name = parts[0].trim(),
                            streamUrl = parts[1].trim(),
                            description = "Internet Radio",
                            imageUrl = if (parts.size > 2) "file:///android_asset/radio_assets/logos/${parts[2].trim()}" else null
                        )
                    } else null
                }
                .filterNotNull()
                .toList()

            groupStations(stations)
        } catch (e: Exception) {
            // Return default stations if file not found
            listOf(
                RadioStation("1", "Rock FM", "https://example.com/rock.mp3", "Rock classics", isFavorite = false),
                RadioStation("2", "Jazz Radio", "https://example.com/jazz.mp3", "Smooth jazz", isFavorite = false),
                RadioStation("3", "News 24", "https://example.com/news.mp3", "Global news", isFavorite = false),
                RadioStation("4", "Pop Hits", "https://example.com/pop.mp3", "Top charts", isFavorite = false),
                RadioStation("5", "LoFi Beats", "https://example.com/lofi.mp3", "Chill study", isFavorite = false)
            )
        }
    }

    private fun groupStations(stations: List<RadioStation>): List<DisplayableItem> {
        val stationGroups = mutableMapOf<String, MutableList<RadioStation>>()
        val pattern = Pattern.compile("^(.*?)(?:\\s*\\(.*\\))?$")

        for (station in stations) {
            val matcher = pattern.matcher(station.name)
            val groupName = if (matcher.find()) {
                matcher.group(1)!!.trim()
            } else {
                station.name
            }
            stationGroups.getOrPut(groupName) { mutableListOf() }.add(station)
        }

        val result = mutableListOf<DisplayableItem>()
        for ((groupName, groupStations) in stationGroups) {
            if (groupStations.size > 1) {
                val mainStation = groupStations.find { it.name == groupName }
                val sortedStations = if (mainStation != null) {
                    listOf(mainStation) + groupStations.filter { it != mainStation }.sortedBy { it.name }
                } else {
                    groupStations.sortedBy { it.name }
                }

                result.add(
                    RadioGroup(
                        id = "group_$groupName",
                        name = groupName,
                        stations = sortedStations,
                        imageUrl = sortedStations.first().imageUrl
                    )
                )
            } else {
                result.addAll(groupStations)
            }
        }
        return result
    }
}
