package com.exory550.exorygallery.presentation.screens.picker

import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import javax.inject.Inject

data class FolderPickerItem(
    val name: String,
    val path: String,
    val coverUri: String?,
    val count: Int
)

@HiltViewModel
class FolderPickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _folders = MutableStateFlow<List<FolderPickerItem>>(emptyList())
    val folders: StateFlow<List<FolderPickerItem>> = _folders
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val filteredFolders: StateFlow<List<FolderPickerItem>> get() = _folders

    init { loadFolders() }

    fun setQuery(q: String) { _query.value = q }

    fun loadFolders() {
        viewModelScope.launch {
            _folders.value = withContext(Dispatchers.IO) {
                val map = mutableMapOf<String, Triple<String, String?, Int>>()
                val cursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.BUCKET_DISPLAY_NAME),
                    null, null, "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
                )
                cursor?.use {
                    val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    val bucketCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                    while (it.moveToNext()) {
                        val path = it.getString(dataCol) ?: continue
                        val bucket = it.getString(bucketCol) ?: "Lainnya"
                        val folderPath = File(path).parent ?: continue
                        val existing = map[folderPath]
                        map[folderPath] = if (existing == null) Triple(bucket, path, 1)
                        else existing.copy(third = existing.third + 1)
                    }
                }
                map.map { (fp, v) -> FolderPickerItem(v.first, fp, v.second, v.third) }
                    .sortedByDescending { it.count }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerScreen(
    navController: NavController,
    title: String = "Pilih tujuan",
    onFolderSelected: (String) -> Unit,
    viewModel: FolderPickerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val folders by viewModel.folders.collectAsState()
    val query by viewModel.query.collectAsState()
    var customPath by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    val filtered = if (query.isBlank()) folders
    else folders.filter { it.name.contains(query, ignoreCase = true) || it.path.contains(query, ignoreCase = true) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(title, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.setQuery(it) },
                    placeholder = { Text("Cari folder") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showCustomInput = !showCustomInput }) {
                        Icon(Icons.Default.CreateNewFolder, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Folder lainnya")
                    }
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Batalkan", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = {
                        if (customPath.isNotBlank()) onFolderSelected(customPath)
                    }) {
                        Text("Oke", fontWeight = FontWeight.Bold)
                    }
                }
                if (showCustomInput) {
                    OutlinedTextField(
                        value = customPath,
                        onValueChange = { customPath = it },
                        label = { Text("Masukkan path folder") },
                        placeholder = { Text("/storage/emulated/0/MyFolder") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (customPath.isNotBlank()) {
                                IconButton(onClick = { onFolderSelected(customPath) }) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50))
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filtered, key = { it.path }) { folder ->
                FolderPickerCard(folder = folder, onClick = { onFolderSelected(folder.path) })
            }
        }
    }
}

@Composable
fun FolderPickerCard(folder: FolderPickerItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (folder.coverUri != null) {
                AsyncImage(
                    model = folder.coverUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Folder, null, modifier = Modifier.size(32.dp))
                }
            }
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 3.dp, vertical = 1.dp)) {
                Text("${folder.count}", color = Color.White, fontSize = 9.sp)
            }
        }
        Text(folder.name, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
    }
}
