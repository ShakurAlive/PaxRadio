package com.pax.radio.data

data class RadioGroup(
    override val id: String,
    override val name: String,
    val stations: List<RadioStation>,
    override val imageUrl: String? = null,
    var isExpanded: Boolean = false
) : DisplayableItem

