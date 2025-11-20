package com.pax.radio.data

import android.content.Context
import com.pax.radio.R
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

object ThemeParser {
    fun parse(context: Context): List<Theme> {
        val themes = mutableListOf<Theme>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = context.resources.getXml(R.xml.themes)
        var eventType = parser.eventType
        var currentTheme: Theme? = null
        var text: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName.equals("theme", ignoreCase = true)) {
                        val name = parser.getAttributeValue(null, "name")
                        val backgroundColor = parser.getAttributeValue(null, "backgroundColor")
                        val primaryTextColor = parser.getAttributeValue(null, "primaryTextColor")
                        val secondaryTextColor = parser.getAttributeValue(null, "secondaryTextColor")
                        val backgroundImage = parser.getAttributeValue(null, "backgroundImage")
                        currentTheme = Theme(name, backgroundColor, primaryTextColor, secondaryTextColor, backgroundImage)
                    }
                }
                XmlPullParser.TEXT -> {
                    text = parser.text
                }
                XmlPullParser.END_TAG -> {
                    if (tagName.equals("theme", ignoreCase = true)) {
                        currentTheme?.let { themes.add(it) }
                    }
                }
            }
            eventType = parser.next()
        }
        return themes
    }
}
