package com.exory550.exorygallery.presentation.screens.tools

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.datasource.FileDataSource
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val fileDataSource: FileDataSource
) : BaseViewModel() {
    private val _convertedPath = MutableStateFlow<String?>(null)
    val convertedPath: StateFlow<String?> = _convertedPath

    fun convert(sourcePath: String, targetFormat: String) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val temp = fileDataSource.createTempFile("converted_", ".")
                _convertedPath.value = temp.absolutePath
            } catch (e: Exception) {
                setError(e.message)
            }
            setLoading(false)
        }
    }
}
