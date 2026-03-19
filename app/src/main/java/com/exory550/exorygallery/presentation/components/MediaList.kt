package com.exory550.exorygallery.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.utils.extensions.toReadableSize

@Composable
fun MediaList(
    mediaList: List<Media>,
    onMediaClick: (Media) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(mediaList, key = { it.id }) { media ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMediaClick(media) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = media.uri,
                    contentDescription = media.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = media.name, style = MaterialTheme.typography.bodyMedium)
                    Text(text = media.size.toReadableSize(), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
