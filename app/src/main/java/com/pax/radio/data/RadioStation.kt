package com.pax.radio.data

data class RadioStation(
    override val id: String,
    override val name: String,
    val streamUrl: String,
    val description: String,
    override val imageUrl: String? = null,
    val isFavorite: Boolean = false
) : DisplayableItem {
    val isValidUrl: Boolean
        get() = streamUrl.isNotBlank() &&
                (streamUrl.startsWith("http://") || streamUrl.startsWith("https://"))
}

