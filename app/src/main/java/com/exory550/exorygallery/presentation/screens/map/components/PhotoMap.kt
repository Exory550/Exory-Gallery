package com.exory550.exorygallery.presentation.screens.map.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.exory550.exorygallery.data.model.Media

@Composable
fun PhotoMap(mediaList: List<Media>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Gunakan tab Explore untuk melihat foto berdasarkan lokasi")
    }
}
