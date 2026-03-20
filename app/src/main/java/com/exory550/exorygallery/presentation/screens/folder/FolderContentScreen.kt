package com.exory550.exorygallery.presentation.screens.folder

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class GalleryItem(
    val path: String,
    val isVideo: Boolean,
    val duration: Long = 0L,
    val extension: String = ""
)

@HiltViewModel
class FolderContentViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _items = MutableStateFlow<List<GalleryItem>>(emptyList())
    val items: StateFlow<List<GalleryItem>> = _items

    fun loadPhotos(folderPath: String) {
        viewModelScope.launch {
            _items.value = scanFolder(context.contentResolver, folderPath)
        }
    }

    private suspend fun scanFolder(cr: ContentResolver, folderPath: String): List<GalleryItem> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<GalleryItem>()
            val imgCursor = cr.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA),
                "${MediaStore.MediaColumns.DATA} LIKE ?",
                arrayOf("$folderPath/%"),
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            imgCursor?.use {
                val col = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                while (it.moveToNext()) {
                    val path = it.getString(col) ?: continue
                    result.add(GalleryItem(path, false, extension = File(path).extension.uppercase()))
                }
            }
            val vidCursor = cr.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Video.Media.DURATION),
                "${MediaStore.MediaColumns.DATA} LIKE ?",
                arrayOf("$folderPath/%"),
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            vidCursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                while (it.moveToNext()) {
                    val path = it.getString(dataCol) ?: continue
                    result.add(GalleryItem(path, true, it.getLong(durCol), File(path).extension.uppercase()))
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
    val photoPaths = remember(items) { items.filter { !it.isVideo }.map { it.path } }
    val gridState = rememberLazyGridState()
    var visibleVideoIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(gridState.firstVisibleItemIndex) {
        val firstVisible = gridState.firstVisibleItemIndex
        val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: firstVisible
        val midIndex = (firstVisible + lastVisible) / 2
        visibleVideoIndex = if (items.getOrNull(midIndex)?.isVideo == true) midIndex else null
    }

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
            state = gridState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(items.size) { index ->
                val item = items[index]
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            if (item.isVideo) {
                                navController.navigate("video/${URLEncoder.encode(item.path, "UTF-8")}")
                            } else {
                                val encodedPath = URLEncoder.encode(item.path, "UTF-8")
                                val encodedAll = photoPaths.joinToString(",") { p ->
                                    URLEncoder.encode(p, "UTF-8")
                                }
                                navController.navigate("photo_pager/$encodedPath?all=$encodedAll")
                            }
                        }
                ) {
                    if (item.isVideo && visibleVideoIndex == index) {
                        AutoplayVideoThumbnail(path = item.path)
                    } else {
                        AsyncImage(
                            model = item.path,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (!item.isVideo && item.extension in listOf("GIF", "RAW", "HEIF", "HEIC", "DNG", "CR2")) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) { Text(item.extension, color = Color.White, fontSize = 9.sp) }
                    }

                    if (item.isVideo) {
                        if (visibleVideoIndex != index) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                            Icon(
                                Icons.Default.PlayCircle, null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(32.dp).align(Alignment.Center)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
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

@Composable
fun AutoplayVideoThumbnail(path: String) {
    val context = LocalContext.current
    val player = remember(path) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(ExoMediaItem.fromUri(Uri.parse(path)))
            prepare()
            playWhenReady = true
            volume = 0f
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }
    DisposableEffect(path) { onDispose { player.release() } }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun formatDuration(ms: Long): String {
    val m = TimeUnit.MILLISECONDS.toMinutes(ms)
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return "%02d:%02d".format(m, s)
}
