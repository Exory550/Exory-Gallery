package com.exory550.exorygallery.presentation.screens.map

import android.content.Context
import android.location.Geocoder
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

data class GeoPhoto(
    val path: String,
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class GeoGroup(
    val address: String,
    val photos: List<GeoPhoto>
)

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _groups = MutableStateFlow<List<GeoGroup>>(emptyList())
    val groups: StateFlow<List<GeoGroup>> = _groups

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadGeoPhotos() }

    fun loadGeoPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            _groups.value = scanGeoPhotos()
            _isLoading.value = false
        }
    }

    private suspend fun scanGeoPhotos(): List<GeoGroup> {
        return withContext(Dispatchers.IO) {
            val photos = mutableListOf<GeoPhoto>()
            val projection = arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE
            )
            val selection = "${MediaStore.Images.Media.LATITUDE} IS NOT NULL AND ${MediaStore.Images.Media.LONGITUDE} IS NOT NULL"
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null,
                "${MediaStore.MediaColumns.DATE_TAKEN} DESC"
            )
            cursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val latCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE)
                val lngCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE)
                while (it.moveToNext()) {
                    val path = it.getString(dataCol) ?: continue
                    val lat = it.getDouble(latCol)
                    val lng = it.getDouble(lngCol)
                    if (lat == 0.0 && lng == 0.0) continue
                    val address = try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        addresses?.firstOrNull()?.let { addr ->
                            addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Tidak diketahui"
                        } ?: "Tidak diketahui"
                    } catch (e: Exception) {
                        "%.4f, %.4f".format(lat, lng)
                    }
                    photos.add(GeoPhoto(path, lat, lng, address))
                }
            }
            photos.groupBy { it.address }
                .map { (addr, list) -> GeoGroup(addr, list) }
                .sortedByDescending { it.photos.size }
        }
    }
}
