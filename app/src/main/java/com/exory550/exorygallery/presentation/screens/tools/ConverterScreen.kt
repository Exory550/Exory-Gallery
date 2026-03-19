package com.exory550.exorygallery.presentation.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold

@Composable
fun ConverterScreen(navController: NavController, viewModel: ConverterViewModel = hiltViewModel()) {
    var selectedFormat by remember { mutableStateOf("jpg") }
    val formats = listOf("jpg", "png", "webp")

    ExoryScaffold(topBar = { TopAppBar(title = { Text("Konverter") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Format Tujuan", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                formats.forEach { fmt ->
                    FilterChip(
                        selected = selectedFormat == fmt,
                        onClick = { selectedFormat = fmt },
                        label = { Text(fmt.uppercase()) }
                    )
                }
            }
            Button(
                onClick = { viewModel.convert("", selectedFormat) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Pilih File & Konversi") }
        }
    }
}
