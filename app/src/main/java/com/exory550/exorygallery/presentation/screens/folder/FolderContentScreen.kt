package com.exory550.exorygallery.presentation.screens.folder

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
import javax.inject.Inject

@HiltViewModel
class FolderContentViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _photos = MutableStateFlow<List<String>>(emptyList())
    val photos: StateFlow<List<String>> = _photos

    fun loadPhotos(folderPath: String) {
        viewModelScope.launch {
            _photos.value = scanFolder(context.contentResolver, folderPath)
        }
    }

    private suspend fun scanFolder(contentResolver: ContentResolver, folderPath: String): List<String> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<String>()
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA
            )
            val selection = "${MediaStore.MediaColumns.DATA} LIKE ?"
            val selectionArgs = arrayOf("$folderPath/%")
            val cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            cursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                while (it.moveToNext()) {
                    result.add(it.getString(dataCol))
                }
            }
            result
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
    LaunchedEffect(folderPath) {
        viewModel.loadPhotos(folderPath)
    }

    val photos by viewModel.photos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
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
            items(photos) { path ->
                AsyncImage(
                    model = path,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.aspectRatio(1f).clickable { }
                )
            }
        }
    }
}
