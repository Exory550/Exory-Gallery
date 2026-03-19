package com.exory550.exorygallery.presentation.screens.statistics

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.repository.GalleryRepository
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository
) : BaseViewModel() {
    private val _mediaCount = MutableStateFlow(0)
    val mediaCount: StateFlow<Int> = _mediaCount

    private val _totalSize = MutableStateFlow(0L)
    val totalSize: StateFlow<Long> = _totalSize

    init {
        viewModelScope.launch {
            _mediaCount.value = galleryRepository.getMediaCount()
            _totalSize.value = galleryRepository.getTotalSize()
        }
    }
}
