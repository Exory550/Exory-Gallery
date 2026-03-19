package com.exory550.exorygallery.presentation.screens.home

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class MediaFolder(
    val name: String,
    val path: String,
    val coverUri: String,
    val count: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _folders = MutableStateFlow<List<MediaFolder>>(emptyList())
    val folders: StateFlow<List<MediaFolder>> = _folders

    init {
        loadFolders()
    }

    fun loadFolders() {
        viewModelScope.launch {
            setLoading(true)
            _folders.value = scanFolders(context.contentResolver)
            setLoading(false)
        }
    }

    private suspend fun scanFolders(contentResolver: ContentResolver): List<MediaFolder> {
        return withContext(Dispatchers.IO) {
            val folderMap = mutableMapOf<String, Triple<String, String, Int>>()
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.BUCKET_ID
            )
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val cursor = contentResolver.query(uri, projection, null, null, "${MediaStore.MediaColumns.DATE_MODIFIED} DESC")
            cursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val bucketCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                while (it.moveToNext()) {
                    val path = it.getString(dataCol) ?: continue
                    val bucket = it.getString(bucketCol) ?: "Lainnya"
                    val folderPath = File(path).parent ?: continue
                    val existing = folderMap[folderPath]
                    if (existing == null) {
                        folderMap[folderPath] = Triple(bucket, path, 1)
                    } else {
                        folderMap[folderPath] = existing.copy(third = existing.third + 1)
                    }
                }
            }
            folderMap.map { (folderPath, value) ->
                MediaFolder(
                    name = value.first,
                    path = folderPath,
                    coverUri = value.second,
                    count = value.third
                )
            }.sortedByDescending { it.count }
        }
    }
}
