package com.pax.radio.data

data class RadioStation(
    val id: String,
    val name: String,
    val streamUrl: String,
    val description: String,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false
) {
    val isValidUrl: Boolean
        get() = streamUrl.isNotBlank() &&
                (streamUrl.startsWith("http://") || streamUrl.startsWith("https://"))
}

