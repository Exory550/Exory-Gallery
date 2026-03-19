package com.exory550.exorygallery.presentation.screens.tools

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.domain.usecases.cleanup.FindDuplicatesUseCase
import com.exory550.exorygallery.domain.usecases.cleanup.FindLargeFilesUseCase
import com.exory550.exorygallery.domain.usecases.cleanup.FindOldMediaUseCase
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CleanupViewModel @Inject constructor(
    private val findDuplicatesUseCase: FindDuplicatesUseCase,
    private val findLargeFilesUseCase: FindLargeFilesUseCase,
    private val findOldMediaUseCase: FindOldMediaUseCase
) : BaseViewModel() {
    private val _duplicates = MutableStateFlow<List<List<Media>>>(emptyList())
    val duplicates: StateFlow<List<List<Media>>> = _duplicates

    private val _largeFiles = MutableStateFlow<List<Media>>(emptyList())
    val largeFiles: StateFlow<List<Media>> = _largeFiles

    private val _oldMedia = MutableStateFlow<List<Media>>(emptyList())
    val oldMedia: StateFlow<List<Media>> = _oldMedia

    fun scan() {
        viewModelScope.launch {
            setLoading(true)
            _duplicates.value = findDuplicatesUseCase()
            _largeFiles.value = findLargeFilesUseCase()
            _oldMedia.value = findOldMediaUseCase()
            setLoading(false)
        }
    }
}
