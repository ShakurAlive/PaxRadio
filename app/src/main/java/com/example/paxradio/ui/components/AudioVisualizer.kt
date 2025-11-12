package com.example.paxradio.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 24,
    maxHeightSegments: Int = 16
) {
    val barHeights = remember { mutableStateListOf<Float>() }
    val colors = remember {
        listOf(
            Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8),
            Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFF64B5F6),
            Color(0xFF4FC3F7), Color(0xFF4DD0E1), Color(0xFF4DB6AC),
            Color(0xFF81C784), Color(0xFFAED581), Color(0xFFFFD54F),
            Color(0xFFFFB74D), Color(0xFFFF8A65), Color(0xFFA1887F),
            Color(0xFF90A4AE)
        ).shuffled()
    }


    LaunchedEffect(Unit) {
        repeat(barCount) {
            barHeights.add(0f)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                for (i in 0 until barCount) {
                    barHeights[i] = Random.nextFloat()
                }
                delay(120) // Slower delay for a more classic feel
            }
        } else {
            for (i in 0 until barCount) {
                barHeights[i] = 0f
            }
        }
    }

    val animatedBarHeights = barHeights.map {
        animateFloatAsState(
            targetValue = if (isPlaying) it else 0f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        ).value
    }

    Canvas(modifier = modifier.fillMaxWidth().height(150.dp)) {
        val segmentSpacing = 2.dp.toPx()
        val totalSpacing = (barCount - 1) * segmentSpacing
        val barWidth = (size.width - totalSpacing) / barCount
        val segmentHeight = size.height / maxHeightSegments

        animatedBarHeights.forEachIndexed { barIndex, height ->
            val activeSegments = (height * maxHeightSegments).toInt()
            val startX = barIndex * (barWidth + segmentSpacing)
            val barColor = colors[barIndex % colors.size]

            for (segmentIndex in 0 until activeSegments) {
                val startY = size.height - (segmentIndex + 1) * segmentHeight
                drawRect(
                    color = barColor.copy(alpha = 1f - (segmentIndex.toFloat() / maxHeightSegments) * 0.5f),
                    topLeft = Offset(startX, startY),
                    size = Size(barWidth, segmentHeight - segmentSpacing)
                )
            }
        }
    }
}
