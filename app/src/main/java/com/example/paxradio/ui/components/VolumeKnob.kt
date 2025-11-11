package com.example.paxradio.ui.components

import android.media.SoundPool
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paxradio.R
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val MAX_ROTATION_DEGREES = 270f

@Composable
fun VolumeKnob(
    volume: Float, // Expected value between 0.0 and 1.0
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var rotation by remember { mutableStateOf(volume * MAX_ROTATION_DEGREES) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "rotation"
    )

    // Synchronize internal rotation state with external volume changes
    LaunchedEffect(volume) {
        rotation = volume.coerceIn(0f, 1f) * MAX_ROTATION_DEGREES
    }

    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val tickSoundId = remember { soundPool.load(context, R.raw.volume_tick, 1) }
    var lastPlayedVolumeStep by remember { mutableStateOf(-1) }

    val knobSize = 200.dp

    Box(
        modifier = modifier.size(knobSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(knobSize)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val touchPos = change.position
                        val angleRad = atan2(
                            center.y - touchPos.y,
                            touchPos.x - center.x
                        )

                        // Map angle to 0-360 range, with 0 degrees at the top
                        val angleDeg = (Math.toDegrees(angleRad.toDouble()).toFloat() + 450f) % 360f

                        // Clamp rotation to the allowed 270-degree arc (from -45 to 225, mapped to 45 to 315)
                        val newRotation = angleDeg.coerceIn(45f, 315f)

                        // Normalize rotation from [45, 315] to [0, 270]
                        val normalizedRotation = newRotation - 45f

                        if (rotation != normalizedRotation) {
                            rotation = normalizedRotation
                            val newVolume = (rotation / MAX_ROTATION_DEGREES).coerceIn(0f, 1f)
                            onVolumeChange(newVolume)

                            // Play tick sound every 5%
                            val currentVolumeStep = (newVolume * 20).toInt() // 100 / 5 = 20 steps
                            if (currentVolumeStep != lastPlayedVolumeStep) {
                                soundPool.play(tickSoundId, 0.5f, 0.5f, 1, 0, 1f)
                                lastPlayedVolumeStep = currentVolumeStep
                            }
                        }
                    }
                }
        ) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 15.dp.toPx()
            val knobRadius = radius * 0.8f

            // 1. Draw the outer scale dots (11 dots for 0% to 100%)
            for (i in 0..10) {
                val angleRad = Math.toRadians((i * (MAX_ROTATION_DEGREES / 10f)) - 135.0).toFloat()
                val dotCenter = Offset(
                    x = centerOffset.x + radius * cos(angleRad),
                    y = centerOffset.y + radius * sin(angleRad)
                )
                drawCircle(
                    color = Color.Gray,
                    radius = 2.dp.toPx(),
                    center = dotCenter
                )
            }

            // 2. Classic Knob Body
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3C3C3C), Color(0xFF2A2A2A)),
                ),
                radius = knobRadius,
                center = centerOffset,
            )
            drawCircle(
                style = Stroke(width = 1.dp.toPx()),
                color = Color.Black.copy(alpha = 0.5f),
                radius = knobRadius,
                center = centerOffset
            )

            // 3. Indicator on the knob
            rotate(degrees = animatedRotation - 135f, pivot = centerOffset) {
                drawLine(
                    color = Color(0xFF00A2FF),
                    start = Offset(centerOffset.x, centerOffset.y - knobRadius * 0.7f),
                    end = Offset(centerOffset.x, centerOffset.y - knobRadius),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Volume percentage text
        Text(
            text = "${(volume * 100).toInt()}%",
            color = Color.White,
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyLarge
        )
    }

    // Release SoundPool when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }
}


