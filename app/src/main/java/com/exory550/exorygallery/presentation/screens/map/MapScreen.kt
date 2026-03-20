package com.exory550.exorygallery.presentation.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exory550.exorygallery.presentation.components.LoadingDialog
import com.exory550.exorygallery.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel = hiltViewModel()) {
    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) LoadingDialog("Memuat lokasi...")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore") },
                actions = {
                    IconButton(onClick = { viewModel.loadGeoPhotos() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang")
                    }
                }
            )
        }
    ) { padding ->
        if (groups.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                    Text(
                        "Tidak ada foto dengan data lokasi",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        "Aktifkan GPS saat mengambil foto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(groups, key = { it.address }) { group ->
                    GeoGroupCard(group = group, onPhotoClick = { path ->
                        navController.navigate(Screen.PhotoViewer.createRoute(path))
                    })
                }
            }
        }
    }
}

@Composable
fun GeoGroupCard(group: GeoGroup, onPhotoClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = group.address,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${group.photos.size} foto",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(group.photos.take(10)) { photo ->
                AsyncImage(
                    model = photo.path,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPhotoClick(photo.path) }
                )
            }
            if (group.photos.size > 10) {
                item {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onPhotoClick(group.photos.first().path) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+${group.photos.size - 10}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp), thickness = 0.5.dp)
    }
}
