package com.exory550.exorygallery.presentation.screens.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold
import com.exory550.exorygallery.utils.extensions.toReadableSize

@Composable
fun StatisticsScreen(navController: NavController, viewModel: StatisticsViewModel = hiltViewModel()) {
    val count by viewModel.mediaCount.collectAsState()
    val size by viewModel.totalSize.collectAsState()

    ExoryScaffold(topBar = { TopAppBar(title = { Text("Statistik") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Media", style = MaterialTheme.typography.labelMedium)
                    Text(" file", style = MaterialTheme.typography.headlineLarge)
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Ukuran", style = MaterialTheme.typography.labelMedium)
                    Text(size.toReadableSize(), style = MaterialTheme.typography.headlineLarge)
                }
            }
        }
    }
}
