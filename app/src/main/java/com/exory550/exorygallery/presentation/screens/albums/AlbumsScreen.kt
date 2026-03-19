package com.exory550.exorygallery.presentation.screens.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.components.ExoryScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(navController: NavController, viewModel: AlbumsViewModel = hiltViewModel()) {
    val albums by viewModel.albums.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var albumName by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Album Baru") },
            text = {
                OutlinedTextField(
                    value = albumName,
                    onValueChange = { albumName = it },
                    label = { Text("Nama Album") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (albumName.isNotBlank()) {
                        viewModel.createAlbum(albumName)
                        albumName = ""
                        showDialog = false
                    }
                }) { Text("Buat") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Batal") }
            }
        )
    }

    ExoryScaffold(
        topBar = { TopAppBar(title = { Text("Album") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Buat Album")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(albums, key = { it.id }) { album ->
                Card(modifier = Modifier.clickable { }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = album.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "${album.mediaCount} item", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
