package com.exory550.exorygallery.presentation.screens.home

import android.content.ContentResolver
import android.content.Context
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
import java.io.File
import javax.inject.Inject




@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _folders = MutableStateFlow<List<MediaFolder>>(emptyList())
    val folders: StateFlow<List<MediaFolder>> = _folders

    private val _timeline = MutableStateFlow<List<TimelineGroup>>(emptyList())
    val timeline: StateFlow<List<TimelineGroup>> = _timeline

    private val _viewMode = MutableStateFlow(ViewMode.GRID_3)
    val viewMode: StateFlow<ViewMode> = _viewMode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadFolders() }

    fun setViewMode(mode: ViewMode) { _viewMode.value = mode }

    fun loadFolders() {
        viewModelScope.launch {
            _isLoading.value = true
            _folders.value = scanFolders(context.contentResolver)
            _timeline.value = buildTimeline(context.contentResolver)
            _isLoading.value = false
        }
    }

    private suspend fun scanFolders(cr: ContentResolver): List<MediaFolder> {
        return withContext(Dispatchers.IO) {
            val map = mutableMapOf<String, Triple<String, String, Int>>()
            val videoFolders = mutableSetOf<String>()

            val imgCursor = cr.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.BUCKET_DISPLAY_NAME),
                null, null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            imgCursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val bucketCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                while (it.moveToNext()) {
                    val path = it.getString(dataCol) ?: continue
                    val bucket = it.getString(bucketCol) ?: "Lainnya"
                    val folderPath = File(path).parent ?: continue
                    val existing = map[folderPath]
                    map[folderPath] = if (existing == null) Triple(bucket, path, 1)
                    else existing.copy(third = existing.third + 1)
                }
            }

            val vidCursor = cr.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.BUCKET_DISPLAY_NAME),
                null, null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            vidCursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val bucketCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                while (it.moveToNext()) {
                    val path = it.getString(dataCol) ?: continue
                    val bucket = it.getString(bucketCol) ?: "Lainnya"
                    val folderPath = File(path).parent ?: continue
                    videoFolders.add(folderPath)
                    val existing = map[folderPath]
                    map[folderPath] = if (existing == null) Triple(bucket, path, 1)
                    else existing.copy(third = existing.third + 1)
                }
            }

            map.map { (fp, v) ->
                MediaFolder(v.first, fp, v.second, v.third, videoFolders.contains(fp))
            }.sortedByDescending { it.count }
        }
    }

    private suspend fun buildTimeline(cr: ContentResolver): List<TimelineGroup> {
        return withContext(Dispatchers.IO) {
            val map = mutableMapOf<String, MutableList<String>>()
            val cursor = cr.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_TAKEN),
                null, null,
                "${MediaStore.MediaColumns.DATE_TAKEN} DESC"
            )
            cursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
                while (it.moveToNext()) {
                    val path = it.getString(dataCol) ?: continue
                    val dateTaken = it.getLong(dateCol)
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = dateTaken }
                    val label = "${cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.getDefault())} ${cal.get(java.util.Calendar.YEAR)}"
                    map.getOrPut(label) { mutableListOf() }.add(path)
                }
            }
            map.map { (label, photos) -> TimelineGroup(label, photos) }
        }
    }
}
