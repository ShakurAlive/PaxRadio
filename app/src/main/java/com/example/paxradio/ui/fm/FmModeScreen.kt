package com.example.paxradio.ui.fm

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Headphones Required",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                        Text(
                            text = "Please plug in wired headphones to use FM Radio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            FmFrequencyDial(
                frequency = frequency,
                onFrequencyChange = onFrequencyChange
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "%.1f MHz".format(frequency),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onScan,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "SCAN STATIONS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun FmFrequencyDial(
    frequency: Float,
    onFrequencyChange: (Float) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(280.dp)) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = size.minDimension / 2f - 40f

            drawCircle(
                color = CardBackground.copy(alpha = 0.6f),
                radius = radius,
                center = Offset(centerX, centerY)
            )

            val needleAngle = ((frequency - 87.5f) / (108.0f - 87.5f) * 270f - 135f)
            rotate(needleAngle, Offset(centerX, centerY)) {
                drawLine(
                    brush = Brush.linearGradient(colors = listOf(primaryColor, secondaryColor)),
                    start = Offset(centerX, centerY),
                    end = Offset(centerX, centerY - radius * 0.8f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            drawCircle(
                color = primaryColor,
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Slider(
            value = frequency,
            onValueChange = onFrequencyChange,
            valueRange = 87.5f..108.0f,
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
            )
        )
    }
}
