package com.exory550.exorygallery.presentation.screens.home

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.domain.usecases.media.GetMediaUseCase
import com.exory550.exorygallery.domain.usecases.media.ScanMediaUseCase
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMediaUseCase: GetMediaUseCase,
    private val scanMediaUseCase: ScanMediaUseCase
) : BaseViewModel() {
    private val _recentMedia = MutableStateFlow<List<Media>>(emptyList())
    val recentMedia: StateFlow<List<Media>> = _recentMedia

    init {
        scanMedia()
        loadRecentMedia()
    }

    private fun scanMedia() {
        viewModelScope.launch {
            setLoading(true)
            try { scanMediaUseCase() } catch (e: Exception) { setError(e.message) }
            setLoading(false)
        }
    }

    private fun loadRecentMedia() {
        viewModelScope.launch {
            getMediaUseCase.allMedia()
                .map { it.take(20) }
                .collect { _recentMedia.value = it }
        }
    }
}
