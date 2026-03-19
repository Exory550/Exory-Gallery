package com.exory550.exorygallery.presentation.screens.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold
import com.exory550.exorygallery.presentation.screens.map.components.PhotoMap

@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel = hiltViewModel()) {
    val geoMedia by viewModel.geoMedia.collectAsState()

    ExoryScaffold(topBar = { TopAppBar(title = { Text("Peta Foto") }) }) { padding ->
        PhotoMap(mediaList = geoMedia, modifier = Modifier.padding(padding).fillMaxSize())
    }
}
