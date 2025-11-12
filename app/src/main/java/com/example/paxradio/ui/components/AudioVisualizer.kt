package com.example.paxradio.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.paxradio.ui.theme.NeonBlue
import com.example.paxradio.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 10,
    barColor: Brush = Brush.verticalGradient(listOf(NeonBlue, NeonPurple)),
    barWidth: Float = 20f,
    barSpacing: Float = 10f
) {
    val barHeights = remember { mutableStateListOf<Float>() }

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
                delay(100)
            }
        }
    }

    val animatedBarHeights = barHeights.map {
        animateFloatAsState(
            targetValue = if (isPlaying) it else 0f,
            animationSpec = tween(durationMillis = 300, easing = LinearEasing)
        ).value
    }

    Canvas(modifier = modifier.fillMaxWidth().height(150.dp)) {
        val totalWidth = (barWidth + barSpacing) * barCount - barSpacing
        val startX = (size.width - totalWidth) / 2

        animatedBarHeights.forEachIndexed { index, height ->
            val x = startX + index * (barWidth + barSpacing)
            val y = size.height * (1 - height)
            drawRoundRect(
                brush = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, size.height * height),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}
