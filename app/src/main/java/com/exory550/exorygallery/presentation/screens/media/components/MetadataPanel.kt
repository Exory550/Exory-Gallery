package com.exory550.exorygallery.presentation.screens.media.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exory550.exorygallery.domain.model.Metadata

@Composable
fun MetadataPanel(metadata: Metadata, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            metadata.make?.let { MetadataRow("Kamera", it) }
            metadata.model?.let { MetadataRow("Model", it) }
            metadata.aperture?.let { MetadataRow("Aperture", it) }
            metadata.shutterSpeed?.let { MetadataRow("Shutter", it) }
            metadata.iso?.let { MetadataRow("ISO", it) }
            metadata.dateTaken?.let { MetadataRow("Tanggal", it) }
            metadata.address?.let { MetadataRow("Lokasi", it) }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
