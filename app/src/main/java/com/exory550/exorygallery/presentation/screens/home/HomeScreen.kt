package com.exory550.exorygallery.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold
import com.exory550.exorygallery.presentation.components.LoadingDialog
import com.exory550.exorygallery.presentation.components.MediaGrid
import com.exory550.exorygallery.presentation.navigation.Screen

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val recentMedia by viewModel.recentMedia.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) LoadingDialog()

    ExoryScaffold(
        topBar = {
            TopAppBar(title = { Text("ExoryGallery") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Terbaru",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            MediaGrid(
                mediaList = recentMedia,
                onMediaClick = { media ->
                    navController.navigate(Screen.MediaViewer.createRoute(media.id))
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
