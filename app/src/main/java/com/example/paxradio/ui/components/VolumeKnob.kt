package com.example.paxradio.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VolumeKnob(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // --- Configuration for Visual Rotation ---
    val minAngle = -PI.toFloat() * 5 / 4
    val maxAngle =  PI.toFloat() * 1 / 4
    val angleRange = maxAngle - minAngle

    // --- State ---
    val animatedAngle = remember { Animatable(minAngle + volume * angleRange) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartVolume by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // --- Synchronization ---
    // Visually update the knob when volume changes from outside
    LaunchedEffect(volume) {
        val targetAngle = minAngle + volume * angleRange
        if (kotlin.math.abs(animatedAngle.value - targetAngle) > 0.01f) {
            animatedAngle.animateTo(targetAngle, animationSpec = spring(stiffness = 50f))
        }
    }

    // --- UI & Gesture Logic: Simplified "Slider" Behavior ---
    Box(
        modifier = modifier
            .size(200.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        // At the start of a drag, just record the initial touch position and volume.
                        dragStartOffset = startOffset
                        dragStartVolume = volume
                    },
                    onDrag = { change, _ ->
                        // Calculate the horizontal distance the finger has moved.
                        val dragDistanceX = change.position.x - dragStartOffset.x

                        // Define sensitivity: how many pixels of drag correspond to a full volume change.
                        // A larger value makes it less sensitive.
                        val fullDragDistance = size.width * 1.5f

                        // Calculate the change in volume based on the horizontal drag distance.
                        val volumeDelta = dragDistanceX / fullDragDistance

                        // Apply the change to the volume that we captured when the drag started.
                        val newVolume = (dragStartVolume + volumeDelta).coerceIn(0f, 1f)

                        // Update the app's state.
                        onVolumeChange(newVolume)

                        // Update the visual indicator to match the new volume.
                        val newIndicatorAngle = minAngle + newVolume * angleRange
                        coroutineScope.launch {
                            animatedAngle.snapTo(newIndicatorAngle)
                        }
                    }
                )
            }
    ) {
        // --- Drawing (Unchanged from the realistic design) ---
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            val knobRadius = radius * 0.8f

            // 1. Draw background scale ticks
            val tickCount = 11
            for (i in 0 until tickCount) {
                val stepRatio = i.toFloat() / (tickCount - 1)
                val stepAngle = minAngle + stepRatio * angleRange
                val startRadius = radius * 0.95f
                val endRadius = radius
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(center.x + startRadius * cos(stepAngle), center.y + startRadius * sin(stepAngle)),
                    end = Offset(center.x + endRadius * cos(stepAngle), center.y + endRadius * sin(stepAngle)),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw knob depression shadow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                    center = center,
                    radius = knobRadius + 4.dp.toPx()
                ),
                radius = knobRadius + 4.dp.toPx(),
                center = center
            )

            // 3. Draw the main knob body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF888888), Color(0xFF444444)),
                    center = center,
                    radius = knobRadius
                ),
                radius = knobRadius,
                center = center
            )
            drawCircle(Color.Black.copy(alpha = 0.8f), knobRadius, center = center, style = Stroke(width = 2.dp.toPx()))

            // 4. Draw the engraved indicator line
            val angle = animatedAngle.value
            val indicatorStartRadius = knobRadius * 0.6f
            val indicatorEndRadius = knobRadius * 0.9f
            drawLine(
                color = Color.Black.copy(alpha = 0.5f),
                start = Offset(center.x + indicatorStartRadius * cos(angle), center.y + indicatorStartRadius * sin(angle)),
                end = Offset(center.x + indicatorEndRadius * cos(angle), center.y + indicatorEndRadius * sin(angle)),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(center.x + indicatorStartRadius * cos(angle + 0.02f), center.y + indicatorStartRadius * sin(angle + 0.02f)),
                end = Offset(center.x + indicatorEndRadius * cos(angle + 0.02f), center.y + indicatorEndRadius * sin(angle + 0.02f)),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
