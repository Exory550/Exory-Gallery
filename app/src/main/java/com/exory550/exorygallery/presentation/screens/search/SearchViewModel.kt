package com.exory550.exorygallery.presentation.screens.search

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SearchResult(
    val name: String,
    val path: String,
    val isFolder: Boolean,
    val count: Int = 0
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.isBlank()) {
                        _results.value = emptyList()
                    } else {
                        search(q)
                    }
                }
        }
    }

    fun setQuery(q: String) { _query.value = q }

    private suspend fun search(query: String) {
        _isLoading.value = true
        _results.value = withContext(Dispatchers.IO) {
            val resultMap = mutableMapOf<String, SearchResult>()
            val projection = arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
            )
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ? OR ${MediaStore.MediaColumns.BUCKET_DISPLAY_NAME} LIKE ?"
            val args = arrayOf("%$query%", "%$query%")

            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, args,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            cursor?.use {
                val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val bucketCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                while (it.moveToNext()) {
                    val path = it.getString(dataCol) ?: continue
                    val name = it.getString(nameCol) ?: continue
                    val bucket = it.getString(bucketCol) ?: "Lainnya"
                    val folderPath = java.io.File(path).parent ?: continue

                    if (name.contains(query, ignoreCase = true)) {
                        resultMap["file_$path"] = SearchResult(
                            name = name,
                            path = path,
                            isFolder = false
                        )
                    }

                    if (bucket.contains(query, ignoreCase = true)) {
                        val existing = resultMap["folder_$folderPath"]
                        resultMap["folder_$folderPath"] = SearchResult(
                            name = bucket,
                            path = folderPath,
                            isFolder = true,
                            count = (existing?.count ?: 0) + 1
                        )
                    }
                }
            }
            resultMap.values.sortedWith(compareByDescending<SearchResult> { it.isFolder }.thenBy { it.name })
        }
        _isLoading.value = false
    }
}
