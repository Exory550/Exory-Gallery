package com.exory550.exorygallery.presentation.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold
import com.exory550.exorygallery.presentation.components.LoadingDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanupScreen(navController: NavController, viewModel: CleanupViewModel = hiltViewModel()) {
    val duplicates by viewModel.duplicates.collectAsState()
    val largeFiles by viewModel.largeFiles.collectAsState()
    val oldMedia by viewModel.oldMedia.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) LoadingDialog("Memindai...")

    ExoryScaffold(topBar = { TopAppBar(title = { Text("Pembersih") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { viewModel.scan() }, modifier = Modifier.fillMaxWidth()) {
                Text("Mulai Pindai")
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Duplikat: ${duplicates.sumOf { it.size }} file")
                    Text("File Besar: ${largeFiles.size} file")
                    Text("File Lama: ${oldMedia.size} file")
                }
            }
        }
    }
}
