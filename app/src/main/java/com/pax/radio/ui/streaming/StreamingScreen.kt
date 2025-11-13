package com.pax.radio.ui.streaming

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// This file is now legacy - main UI moved to MainActivity
@Composable
fun StreamingScreen(vm: StreamingViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Radio UI has been redesigned. See MainActivity.")
    }
}

