package com.exory550.exorygallery.presentation.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold
import com.exory550.exorygallery.presentation.components.MediaGrid
import com.exory550.exorygallery.presentation.navigation.Screen

@Composable
fun SearchScreen(navController: NavController, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    ExoryScaffold(
        topBar = { TopAppBar(title = { Text("Cari") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setQuery(it) },
                label = { Text("Cari foto...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true
            )
            MediaGrid(
                mediaList = results,
                onMediaClick = { navController.navigate(Screen.MediaViewer.createRoute(it.id)) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
