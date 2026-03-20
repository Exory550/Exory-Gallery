package com.exory550.exorygallery.presentation.screens.albums

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(navController: NavController, viewModel: AlbumsViewModel = hiltViewModel()) {
    val albums by viewModel.albums.collectAsState()
    val smartAlbums by viewModel.smartAlbums.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedAlbum by remember { mutableStateOf<AlbumUiModel?>(null) }
    var showAlbumMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Album Baru") },
            text = {
                OutlinedTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it },
                    label = { Text("Nama Album") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newAlbumName.isNotBlank()) {
                        viewModel.createAlbum(newAlbumName)
                        newAlbumName = ""
                        showCreateDialog = false
                    }
                }) { Text("Buat") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Batal") } }
        )
    }

    if (showRenameDialog && selectedAlbum != null) {
        var renameText by remember { mutableStateOf(selectedAlbum!!.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Ganti Nama") },
            text = {
                OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameAlbum(selectedAlbum!!.id, renameText)
                    showRenameDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Batal") } }
        )
    }

    if (showMergeDialog && selectedAlbum != null) {
        var mergeTarget by remember { mutableStateOf<AlbumUiModel?>(null) }
        var mergeName by remember { mutableStateOf("Album Gabungan") }
        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            title = { Text("Gabung Album") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih album tujuan:", style = MaterialTheme.typography.labelMedium)
                    LazyColumn(modifier = Modifier.height(150.dp)) {
                        items(albums.filter { it.id != selectedAlbum!!.id }) { album ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { mergeTarget = album }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = mergeTarget?.id == album.id, onClick = { mergeTarget = album })
                                Text(album.name, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = mergeName,
                        onValueChange = { mergeName = it },
                        label = { Text("Nama Album Baru") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mergeTarget?.let { target ->
                        viewModel.mergeAlbums(selectedAlbum!!.id, target.id, mergeName)
                    }
                    showMergeDialog = false
                }) { Text("Gabung") }
            },
            dismissButton = { TextButton(onClick = { showMergeDialog = false }) { Text("Batal") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Album") },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, null)
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Nama ${if (sortOrder == AlbumSortOrder.NAME) "✓" else ""}") },
                            onClick = { viewModel.setSortOrder(AlbumSortOrder.NAME); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.SortByAlpha, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Tanggal ${if (sortOrder == AlbumSortOrder.DATE) "✓" else ""}") },
                            onClick = { viewModel.setSortOrder(AlbumSortOrder.DATE); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.DateRange, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Jumlah Foto ${if (sortOrder == AlbumSortOrder.COUNT) "✓" else ""}") },
                            onClick = { viewModel.setSortOrder(AlbumSortOrder.COUNT); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.PhotoLibrary, null) }
                        )
                    }
                    IconButton(onClick = { showCreateDialog = true; newAlbumName = "" }) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (smartAlbums.isNotEmpty()) {
                item {
                    Text(
                        "Album Smart",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(smartAlbums) { album ->
                            SmartAlbumCard(album = album)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                }
            }

            val pinned = albums.filter { it.isPinned }
            val unpinned = albums.filter { !it.isPinned }

            if (pinned.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PushPin, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Disematkan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 1000.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(pinned, key = { it.id }) { album ->
                            AlbumCard(
                                album = album,
                                onLongClick = { selectedAlbum = album; showAlbumMenu = true },
                                onClick = {}
                            )
                        }
                    }
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            }

            if (unpinned.isNotEmpty()) {
                item {
                    Text(
                        "Semua Album",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 3000.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(unpinned, key = { it.id }) { album ->
                            AlbumCard(
                                album = album,
                                onLongClick = { selectedAlbum = album; showAlbumMenu = true },
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAlbumMenu && selectedAlbum != null) {
        ModalBottomSheet(onDismissRequest = { showAlbumMenu = false }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    selectedAlbum!!.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(if (selectedAlbum!!.isPinned) "Lepas Sematan" else "Sematkan ke Atas") },
                    leadingContent = { Icon(Icons.Default.PushPin, null) },
                    modifier = Modifier.clickable { viewModel.togglePin(selectedAlbum!!.id); showAlbumMenu = false }
                )
                ListItem(
                    headlineContent = { Text("Ganti Nama") },
                    leadingContent = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.clickable { showRenameDialog = true; showAlbumMenu = false }
                )
                ListItem(
                    headlineContent = { Text("Gabung dengan Album Lain") },
                    leadingContent = { Icon(Icons.Default.MergeType, null) },
                    modifier = Modifier.clickable { showMergeDialog = true; showAlbumMenu = false }
                )
                ListItem(
                    headlineContent = { Text("Hapus Album", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { viewModel.deleteAlbum(selectedAlbum!!.id); showAlbumMenu = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumCard(album: AlbumUiModel, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        if (album.coverUri != null) {
            AsyncImage(
                model = album.coverUri,
                contentDescription = album.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
        if (album.isPinned) {
            Icon(
                Icons.Default.PushPin, null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(18.dp).align(Alignment.TopEnd).padding(6.dp)
            )
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
            Text(album.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${album.photoCount} foto", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}

@Composable
fun SmartAlbumCard(album: AlbumUiModel) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        if (album.coverUri != null) {
            AsyncImage(
                model = album.coverUri,
                contentDescription = album.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        Icon(
            Icons.Default.AutoAwesome, null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).padding(4.dp)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)) {
            Text(album.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${album.photoCount}", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
        }
    }
}
