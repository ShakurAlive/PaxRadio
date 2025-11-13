package com.pax.radio.ui.fm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FmRadioScreen(vm: FmRadioViewModel) {
    val supported by vm.supported.collectAsState()
    val headphones by vm.headphones.collectAsState()
    val freq by vm.freq.collectAsState()
    val scanned by vm.scanned.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("FM Radio", style = MaterialTheme.typography.headlineSmall)
        when {
            !supported -> { Text("FM not supported on this device."); return }
            !headphones -> { Text("Plug wired headphones (antenna required)."); Button(onClick = vm::refreshHeadphones){ Text("Recheck") }; return }
        }
        Text("Frequency: %.1f MHz".format(freq))
        Slider(value = freq, onValueChange = vm::setFreq, valueRange = 87.5f..108f)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = vm::scan) { Text("Scan") }
            Button(onClick = vm::refreshHeadphones) { Text("Refresh") }
        }
        Text("Scanned Stations")
        LazyColumn(Modifier.weight(1f)) {
            scanned.forEach { f ->
                item(f) {
                    ListItem(
                        headlineContent = { Text("%.1f MHz".format(f)) },
                        modifier = Modifier.clickable { vm.setFreq(f) }
                    )
                    HorizontalDivider()
                }
            }
        }
        Text("Note: FM playback simulated.")
    }
}

