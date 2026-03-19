package com.exory550.exorygallery.presentation.screens.map.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.exory550.exorygallery.data.model.Media
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun PhotoMap(mediaList: List<Media>, modifier: Modifier = Modifier) {
    val defaultPos = if (mediaList.isNotEmpty() && mediaList.first().latitude != null)
        LatLng(mediaList.first().latitude!!, mediaList.first().longitude!!)
    else LatLng(-6.2, 106.8)

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 8f)
    }

    GoogleMap(modifier = modifier, cameraPositionState = cameraState) {
        mediaList.filter { it.hasLocation }.forEach { media ->
            Marker(
                state = MarkerState(position = LatLng(media.latitude!!, media.longitude!!)),
                title = media.name
            )
        }
    }
}
