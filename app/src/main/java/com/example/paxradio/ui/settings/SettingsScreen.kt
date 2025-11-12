package com.example.paxradio.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paxradio.data.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        containerColor = Color.Black.copy(alpha = 0.5f), // Semi-transparent background
    ) { padding ->
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                // Settings Items
                var selectedTabIndex by remember { mutableStateOf(0) }
                val tabs = listOf("Appearance", "General", "About")

                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                when (selectedTabIndex) {
                    0 -> AppearanceSettings(viewModel = viewModel)
                    1 -> GeneralSettings()
                    2 -> AboutSettings()
                }
            }
        }
    }
}

@Composable
private fun AppearanceSettings(viewModel: SettingsViewModel) {
    val currentTheme by viewModel.theme.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Select Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(AppTheme.values()) { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setTheme(theme) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(theme.name.lowercase().replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.bodyLarge)
                RadioButton(
                    selected = currentTheme == theme,
                    onClick = { viewModel.setTheme(theme) }
                )
            }
        }
    }
}

@Composable
private fun GeneralSettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("General Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        // Add general settings here later
    }
}

@Composable
private fun AboutSettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        // Add about info here later
    }
}
