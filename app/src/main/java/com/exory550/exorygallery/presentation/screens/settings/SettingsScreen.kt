package com.exory550.exorygallery.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold

@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsState()
    val gridColumns by viewModel.gridColumns.collectAsState()

    ExoryScaffold(topBar = { TopAppBar(title = { Text("Pengaturan") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("light", "dark", "system").forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { viewModel.setTheme(mode) },
                        label = { Text(mode.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Kolom Grid: ", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = gridColumns.toFloat(),
                    onValueChange = { viewModel.setGridColumns(it.toInt()) },
                    valueRange = 2f..5f,
                    steps = 2,
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }
        }
    }
}
