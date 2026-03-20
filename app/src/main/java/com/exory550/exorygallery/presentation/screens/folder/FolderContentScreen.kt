package com.exory550.exorygallery.presentation.screens.folder

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class MediaItem(
    val path: String,
    val isVideo: Boolean,
    val duration: Long = 0L,
    val extension: String = ""
)

@HiltViewModel
class FolderContentViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _items = MutableStateFlow<List<MediaItem>>(emptyList())
    val items: StateFlow<List<MediaItem>> = _items

    fun loadPhotos(folderPath: String) {
        viewModelScope.launch {
            _items.value = scanFolder(context.contentResolver, folderPath)
        }
    }

    private suspend fun scanFolder(cr: ContentResolver, folderPath: String): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<MediaItem>()
            val imgProjection = arrayOf(MediaStore.MediaColumns.DATA)
            val imgCursor = cr.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imgProjection,
                "${MediaStore.MediaColumns.DATA} LIKE ?", arrayOf("$folderPath/%"),
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            imgCursor?.use {
                val col = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                while (it.moveToNext()) {
                    val path = it.getString(col) ?: return@use
                    result.add(MediaItem(path, false, extension = File(path).extension.uppercase()))
                }
            }
            val vidProjection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Video.Media.DURATION)
            val vidCursor = cr.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, vidProjection,
                "${MediaStore.MediaColumns.DATA} LIKE ?", arrayOf("$folderPath/%"),
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            vidCursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                while (it.moveToNext()) {
                    val path = it.getString(dataCol) ?: return@use
                    val dur = it.getLong(durCol)
                    result.add(MediaItem(path, true, dur, File(path).extension.uppercase()))
                }
            }
            result.sortedByDescending { File(it.path).lastModified() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderContentScreen(
    navController: NavController,
    folderName: String,
    folderPath: String,
    viewModel: FolderContentViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    LaunchedEffect(folderPath) { viewModel.loadPhotos(folderPath) }
    val items by viewModel.items.collectAsState()
    val photoPaths = items.filter { !it.isVideo }.map { it.path }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(items) { item ->
                Box(modifier = Modifier.aspectRatio(1f).clickable {
                    if (item.isVideo) {
                        val encoded = java.net.URLEncoder.encode(item.path, "UTF-8")
                        navController.navigate("video/$encoded")
                    } else {
                        val encoded = java.net.URLEncoder.encode(item.path, "UTF-8")
                        val allEncoded = photoPaths.map { java.net.URLEncoder.encode(it, "UTF-8") }
                        navController.navigate("photo_pager/$encoded")
                    }
                }) {
                    AsyncImage(
                        model = item.path,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    val badge = when {
                        item.isVideo -> null
                        item.extension in listOf("GIF", "RAW", "HEIF", "HEIC", "DNG", "CR2") -> item.extension
                        else -> null
                    }
                    badge?.let {
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) { Text(it, color = Color.White, fontSize = 9.sp) }
                    }
                    if (item.isVideo) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                        Icon(
                            Icons.Default.PlayCircle, null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(32.dp).align(Alignment.Center)
                        )
                        Box(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(formatDuration(item.duration), color = Color.White, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val m = TimeUnit.MILLISECONDS.toMinutes(ms)
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return "%02d:%02d".format(m, s)
}
