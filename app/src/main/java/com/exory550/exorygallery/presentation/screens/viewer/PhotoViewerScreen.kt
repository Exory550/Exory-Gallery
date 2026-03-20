package com.exory550.exorygallery.presentation.screens.viewer

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exory550.exorygallery.presentation.navigation.Screen
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLDecoder

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    navController: NavController,
    encodedPath: String,
    allPhotos: List<String> = emptyList()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val initialPath = URLDecoder.decode(encodedPath, "UTF-8")
    val photos = allPhotos.ifEmpty { listOf(initialPath) }
    val initialIndex = photos.indexOf(initialPath).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }

    var showControls by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showProperties by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }
    val snackbarState = remember { SnackbarHostState() }
    val currentPath = photos.getOrElse(pagerState.currentPage) { initialPath }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { snackbarState.showSnackbar(it); snackbarMsg = null }
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Foto") },
            text = { Text("Hapus ${File(currentPath).name}?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val ok = PhotoActionsHelper.deleteFile(context, currentPath)
                        snackbarMsg = if (ok) "Foto dihapus" else "Gagal menghapus"
                        showDeleteConfirm = false
                        if (ok && photos.size == 1) navController.popBackStack()
                    }
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") } }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Ubah Nama") },
            text = {
                OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true, label = { Text("Nama file") })
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val newPath = PhotoActionsHelper.renameFile(currentPath, renameText)
                        snackbarMsg = if (newPath != null) "Nama diubah" else "Gagal"
                        showRenameDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Batal") } }
        )
    }

    if (showMenu) {
        PhotoMenuSheet(
            path = currentPath,
            context = context,
            onDismiss = { showMenu = false },
            onRotate = { snackbarMsg = "Gunakan fitur Edit untuk rotasi" },
            onProperties = { showProperties = true },
            onRename = { renameText = File(currentPath).nameWithoutExtension; showRenameDialog = true },
            onHide = {
                scope.launch {
                    val ok = PhotoActionsHelper.hideFile(currentPath)
                    snackbarMsg = if (ok) "File disembunyikan" else "Gagal"
                }
            },
            onCopyTo = {
                navController.navigate(
                    Screen.FolderPicker.createRoute("copy", java.net.URLEncoder.encode(currentPath, "UTF-8"))
                )
            },
            onMoveTo = {
                navController.navigate(
                    Screen.FolderPicker.createRoute("move", java.net.URLEncoder.encode(currentPath, "UTF-8"))
                )
            },
            onEdit = { navController.navigate(Screen.ImageEditor.createRoute(currentPath)) },
            onShare = { PhotoActionsHelper.shareFile(context, currentPath) },
            onOpenWith = { PhotoActionsHelper.openWith(context, currentPath) },
            onSetAs = { PhotoActionsHelper.setAsWallpaper(context, currentPath) },
            onAddFavorite = { snackbarMsg = "Ditambahkan ke favorit" },
            onSelectAll = {},
            onDelete = { showDeleteConfirm = true }
        )
    }

    if (showProperties) {
        PhotoPropertiesSheet(path = currentPath, onDismiss = { showProperties = false })
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarState) }, containerColor = Color.Black) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), key = { photos[it] }) { page ->
                ZoomableImage(path = photos[page], onTap = { showControls = !showControls }, pagerState = pagerState)
            }

            AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.TopStart).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)).padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${pagerState.currentPage + 1} / ${photos.size}", color = Color.White)
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                }
            }

            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)).padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomAction(Icons.Default.Tune, "Edit") { navController.navigate(Screen.ImageEditor.createRoute(currentPath)) }
                    BottomAction(Icons.Default.Share, "Bagikan") { PhotoActionsHelper.shareFile(context, currentPath) }
                    BottomAction(Icons.Default.Info, "Properti") { showProperties = true }
                    BottomAction(Icons.Default.DriveFileMove, "Pindah") {
                        navController.navigate(Screen.FolderPicker.createRoute("move", java.net.URLEncoder.encode(currentPath, "UTF-8")))
                    }
                    BottomAction(Icons.Default.Delete, "Hapus") { showDeleteConfirm = true }
                }
            }
        }
    }
}

@Composable
fun BottomAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp).clickable { onClick() }) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableImage(path: String, onTap: () -> Unit, pagerState: PagerState) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val animatedScale by animateFloatAsState(targetValue = scale, animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "scale")
    LaunchedEffect(pagerState.currentPage) { scale = 1f; offsetX = 0f; offsetY = 0f }

    Box(
        modifier = Modifier.fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { if (scale > 1f) { scale = 1f; offsetX = 0f; offsetY = 0f } else scale = 2.5f },
                    onTap = { onTap() }
                )
            }
            .pointerInput(scale) {
                if (scale > 1f) detectTransformGestures { _, pan, zoom, _ -> scale = (scale * zoom).coerceIn(1f, 5f); offsetX += pan.x; offsetY += pan.y }
                else detectTransformGestures { _, _, zoom, _ -> if (zoom > 1f) scale = (scale * zoom).coerceIn(1f, 5f) }
            }
    ) {
        AsyncImage(
            model = path, contentDescription = null, contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().align(Alignment.Center)
                .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale, translationX = offsetX, translationY = offsetY)
        )
    }
}
