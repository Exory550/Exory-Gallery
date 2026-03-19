package com.exory550.exorygallery.presentation.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold
import com.exory550.exorygallery.presentation.components.MediaGrid
import com.exory550.exorygallery.presentation.navigation.Screen

@Composable
fun VaultScreen(navController: NavController, viewModel: VaultViewModel = hiltViewModel()) {
    val vaultItems by viewModel.vaultItems.collectAsState()

    ExoryScaffold(topBar = { TopAppBar(title = { Text("Vault") }) }) { padding ->
        MediaGrid(
            mediaList = vaultItems,
            onMediaClick = { navController.navigate(Screen.MediaViewer.createRoute(it.id)) },
            modifier = Modifier.padding(padding).fillMaxSize()
        )
    }
}
