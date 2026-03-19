package com.exory550.exorygallery.presentation.screens.media

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold
import com.exory550.exorygallery.presentation.screens.media.components.ImageViewer
import com.exory550.exorygallery.presentation.screens.media.components.MetadataPanel
import com.exory550.exorygallery.presentation.screens.media.components.VideoPlayer

@Composable
fun MediaViewerScreen(
    navController: NavController,
    mediaId: Long,
    viewModel: MediaViewModel = hiltViewModel()
) {
    LaunchedEffect(mediaId) { viewModel.loadMedia(mediaId) }

    val media by viewModel.media.collectAsState()
    val metadata by viewModel.metadata.collectAsState()
    var showMetadata by remember { mutableStateOf(false) }

    ExoryScaffold(
        topBar = {
            TopAppBar(
                title = { Text(media?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            media?.let { m ->
                if (m.isVideo) VideoPlayer(uri = m.uri)
                else ImageViewer(uri = m.uri)

                if (showMetadata) metadata?.let { MetadataPanel(metadata = it) }

                TextButton(onClick = { showMetadata = !showMetadata }) {
                    Text(if (showMetadata) "Sembunyikan Metadata" else "Lihat Metadata")
                }
            }
        }
    }
}
