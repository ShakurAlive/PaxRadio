package com.example.paxradio.ui.fm

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paxradio.ui.theme.CardBackground
import com.example.paxradio.ui.theme.DeepBlue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun FmModeScreen(
    hasHeadphones: Boolean,
    frequency: Float,
    onFrequencyChange: (Float) -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasHeadphones) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF663300))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ Headphones Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please plug in wired headphones to use FM Radio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFCCBB)
                    )
                }
            }
        } else {
            // FM Frequency Dial
            FmFrequencyDial(
                frequency = frequency,
                onFrequencyChange = onFrequencyChange
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Frequency Display
            Text(
                text = "%.1f MHz".format(frequency),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = DeepBlue,
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Scan Button
            Button(
                onClick = onScan,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepBlue
                )
            ) {
                Text(
                    text = "SCAN STATIONS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Note: FM playback simulated",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
private fun FmFrequencyDial(
    frequency: Float,
    onFrequencyChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Analog-style frequency dial
        Canvas(
            modifier = Modifier.size(280.dp)
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = size.minDimension / 2f - 40f

            // Draw dial background
            drawCircle(
                color = CardBackground,
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Draw frequency marks
            for (i in 0..40) {
                val freq = 87.5f + (i * 0.5f)
                val angle = (freq - 87.5f) / (108.0f - 87.5f) * 270f - 135f
                val angleRad = angle * PI.toFloat() / 180f

                val startRadius = if (i % 2 == 0) radius - 20f else radius - 10f
                val endRadius = radius

                val startX = centerX + cos(angleRad) * startRadius
                val startY = centerY + sin(angleRad) * startRadius
                val endX = centerX + cos(angleRad) * endRadius
                val endY = centerY + sin(angleRad) * endRadius

                drawLine(
                    color = Color(0xFF666666),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Draw needle
            val needleAngle = ((frequency - 87.5f) / (108.0f - 87.5f) * 270f - 135f)
            rotate(needleAngle, Offset(centerX, centerY)) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0066CC), Color(0xFF0088FF))
                    ),
                    start = Offset(centerX, centerY),
                    end = Offset(centerX, centerY - radius * 0.8f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Center dot
            drawCircle(
                color = Color(0xFF0066CC),
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Frequency slider
        Slider(
            value = frequency,
            onValueChange = onFrequencyChange,
            valueRange = 87.5f..108.0f,
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = SliderDefaults.colors(
                thumbColor = DeepBlue,
                activeTrackColor = DeepBlue,
                inactiveTrackColor = Color(0xFF3A3A3A)
            )
        )
    }
}

