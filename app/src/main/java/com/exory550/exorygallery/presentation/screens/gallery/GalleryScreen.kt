package com.exory550.exorygallery.presentation.screens.gallery

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
fun GalleryScreen(navController: NavController, viewModel: GalleryViewModel = hiltViewModel()) {
    val media by viewModel.media.collectAsState()
    val filter by viewModel.filter.collectAsState()

    ExoryScaffold(
        topBar = {
            TopAppBar(title = { Text("Galeri") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = listOf("all", "images", "videos").indexOf(filter)) {
                listOf("Semua", "Foto", "Video").forEachIndexed { i, title ->
                    Tab(
                        selected = listOf("all", "images", "videos")[i] == filter,
                        onClick = { viewModel.setFilter(listOf("all", "images", "videos")[i]) },
                        text = { Text(title) }
                    )
                }
            }
            MediaGrid(
                mediaList = media,
                onMediaClick = { navController.navigate(Screen.MediaViewer.createRoute(it.id)) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
