package com.exory550.exorygallery.presentation.screens.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exory550.exorygallery.presentation.components.LoadingDialog
import com.exory550.exorygallery.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val folders by viewModel.folders.collectAsState()
    val timeline by viewModel.timeline.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var gridColumns by remember { mutableStateOf(3) }
    var pinchScale by remember { mutableStateOf(1f) }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    else
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) viewModel.loadFolders()
    }

    LaunchedEffect(Unit) { launcher.launch(permissions) }

    if (isLoading) LoadingDialog("Memuat...")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ExoryGallery", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Grid 2") }, onClick = { viewModel.setViewMode(ViewMode.GRID_2); gridColumns = 2; showMenu = false }, leadingIcon = { Icon(Icons.Default.ViewModule, null) })
                        DropdownMenuItem(text = { Text("Grid 3") }, onClick = { viewModel.setViewMode(ViewMode.GRID_3); gridColumns = 3; showMenu = false }, leadingIcon = { Icon(Icons.Default.ViewModule, null) })
                        DropdownMenuItem(text = { Text("Grid 4") }, onClick = { viewModel.setViewMode(ViewMode.GRID_4); gridColumns = 4; showMenu = false }, leadingIcon = { Icon(Icons.Default.ViewComfy, null) })
                        DropdownMenuItem(text = { Text("Timeline") }, onClick = { viewModel.setViewMode(ViewMode.TIMELINE); showMenu = false }, leadingIcon = { Icon(Icons.Default.ViewAgenda, null) })
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (folders.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tidak ada foto ditemukan", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { launcher.launch(permissions) }) { Text("Izinkan Akses") }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            pinchScale *= zoom
                            when {
                                pinchScale < 0.75f -> { gridColumns = (gridColumns + 1).coerceAtMost(4); pinchScale = 1f }
                                pinchScale > 1.4f -> { gridColumns = (gridColumns - 1).coerceAtLeast(2); pinchScale = 1f }
                            }
                        }
                    }
            ) {
                if (viewMode == ViewMode.TIMELINE) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        timeline.forEach { group ->
                            stickyHeader {
                                Text(
                                    text = group.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(gridColumns),
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 2000.dp),
                                    contentPadding = PaddingValues(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(group.photos) { path ->
                                        AsyncImage(
                                            model = path,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.aspectRatio(1f).clickable {
                                                navController.navigate(Screen.PhotoViewer.createRoute(path))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(folders, key = { it.path }) { folder ->
                            FolderCard(folder = folder, onClick = {
                                navController.navigate(Screen.FolderContent.createRoute(folder.name, folder.path))
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderCard(folder: MediaFolder, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = folder.coverUri,
            contentDescription = folder.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

        if (folder.hasVideo) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(36.dp).align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = folder.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${folder.count}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}
