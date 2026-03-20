package com.exory550.exorygallery.presentation.screens.albums

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.local.database.entities.AlbumEntity
import com.exory550.exorygallery.data.repository.AlbumRepository
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class AlbumSortOrder { NAME, DATE, COUNT }

data class AlbumUiModel(
    val id: Long,
    val name: String,
    val coverUri: String?,
    val photoCount: Int,
    val createdAt: Long,
    val isPinned: Boolean,
    val isSmartAlbum: Boolean = false
)

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _sortOrder = MutableStateFlow(AlbumSortOrder.NAME)
    val sortOrder: StateFlow<AlbumSortOrder> = _sortOrder

    private val _albums = MutableStateFlow<List<AlbumUiModel>>(emptyList())
    val albums: StateFlow<List<AlbumUiModel>> = _albums

    private val _smartAlbums = MutableStateFlow<List<AlbumUiModel>>(emptyList())
    val smartAlbums: StateFlow<List<AlbumUiModel>> = _smartAlbums

    init {
        viewModelScope.launch {
            combine(albumRepository.getAllAlbums(), _sortOrder) { list, sort ->
                val mapped = list.map { album ->
                    val count = albumRepository.getMediaCountForAlbum(album.id)
                    AlbumUiModel(
                        id = album.id,
                        name = album.name,
                        coverUri = album.coverUri,
                        photoCount = count,
                        createdAt = album.createdAt,
                        isPinned = album.sortOrder == -1
                    )
                }
                when (sort) {
                    AlbumSortOrder.NAME -> mapped.sortedWith(compareByDescending<AlbumUiModel> { it.isPinned }.thenBy { it.name })
                    AlbumSortOrder.DATE -> mapped.sortedWith(compareByDescending<AlbumUiModel> { it.isPinned }.thenByDescending { it.createdAt })
                    AlbumSortOrder.COUNT -> mapped.sortedWith(compareByDescending<AlbumUiModel> { it.isPinned }.thenByDescending { it.photoCount })
                }
            }.collect { _albums.value = it }
        }
        loadSmartAlbums()
    }

    fun setSortOrder(order: AlbumSortOrder) { _sortOrder.value = order }

    fun createAlbum(name: String) {
        viewModelScope.launch {
            try { albumRepository.createAlbum(name) } catch (e: Exception) { setError(e.message) }
        }
    }

    fun renameAlbum(albumId: Long, newName: String) {
        viewModelScope.launch {
            try {
                val album = albumRepository.getAlbumById(albumId) ?: return@launch
                albumRepository.updateAlbum(album.copy(name = newName, updatedAt = System.currentTimeMillis()))
            } catch (e: Exception) { setError(e.message) }
        }
    }

    fun deleteAlbum(albumId: Long) {
        viewModelScope.launch {
            try {
                val album = albumRepository.getAlbumById(albumId) ?: return@launch
                albumRepository.deleteAlbum(album)
            } catch (e: Exception) { setError(e.message) }
        }
    }

    fun togglePin(albumId: Long) {
        viewModelScope.launch {
            try {
                val album = albumRepository.getAlbumById(albumId) ?: return@launch
                val newOrder = if (album.sortOrder == -1) 0 else -1
                albumRepository.updateAlbum(album.copy(sortOrder = newOrder, updatedAt = System.currentTimeMillis()))
            } catch (e: Exception) { setError(e.message) }
        }
    }

    fun setCover(albumId: Long, coverUri: String) {
        viewModelScope.launch {
            try {
                val album = albumRepository.getAlbumById(albumId) ?: return@launch
                albumRepository.updateAlbum(album.copy(coverUri = coverUri, updatedAt = System.currentTimeMillis()))
            } catch (e: Exception) { setError(e.message) }
        }
    }

    fun mergeAlbums(sourceId: Long, targetId: Long, newName: String) {
        viewModelScope.launch {
            try {
                albumRepository.createAlbum(newName)
            } catch (e: Exception) { setError(e.message) }
        }
    }

    private fun loadSmartAlbums() {
        viewModelScope.launch {
            _smartAlbums.value = buildSmartAlbums()
        }
    }

    private suspend fun buildSmartAlbums(): List<AlbumUiModel> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<AlbumUiModel>()
            val locationMap = mutableMapOf<String, Pair<String, Int>>()
            val monthMap = mutableMapOf<String, Pair<String, Int>>()

            val projection = arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.MediaColumns.DATE_TAKEN
            )
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                "${MediaStore.MediaColumns.DATE_TAKEN} DESC"
            )
            cursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val latCol = runCatching { it.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE) }.getOrDefault(-1)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
                while (it.moveToNext()) {
                    val path = it.getString(dataCol) ?: continue
                    val dateTaken = it.getLong(dateCol)
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = dateTaken }
                    val monthLabel = "${cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.getDefault())} ${cal.get(java.util.Calendar.YEAR)}"
                    val existing = monthMap[monthLabel]
                    monthMap[monthLabel] = if (existing == null) Pair(path, 1) else Pair(existing.first, existing.second + 1)

                    if (latCol >= 0) {
                        val lat = it.getDouble(latCol)
                        if (lat != 0.0) {
                            val locKey = "%.1f".format(lat)
                            val existingLoc = locationMap[locKey]
                            locationMap[locKey] = if (existingLoc == null) Pair(path, 1) else Pair(existingLoc.first, existingLoc.second + 1)
                        }
                    }
                }
            }

            monthMap.entries.take(6).forEachIndexed { i, (label, data) ->
                result.add(AlbumUiModel(id = -(i + 1).toLong(), name = label, coverUri = data.first, photoCount = data.second, createdAt = 0, isPinned = false, isSmartAlbum = true))
            }

            if (locationMap.isNotEmpty()) {
                val topLoc = locationMap.maxByOrNull { it.value.second }
                topLoc?.let {
                    result.add(AlbumUiModel(id = -100, name = "Foto Berlokasi", coverUri = it.value.first, photoCount = it.value.second, createdAt = 0, isPinned = false, isSmartAlbum = true))
                }
            }

            result
        }
    }
}
