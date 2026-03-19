package com.exory550.exorygallery.presentation.screens.gallery

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.domain.usecases.media.DeleteMediaUseCase
import com.exory550.exorygallery.domain.usecases.media.GetMediaUseCase
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getMediaUseCase: GetMediaUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase
) : BaseViewModel() {
    private val _filter = MutableStateFlow("all")
    val filter: StateFlow<String> = _filter

    val media: StateFlow<List<Media>> = _filter.flatMapLatest { f ->
        when (f) {
            "images" -> getMediaUseCase.images()
            "videos" -> getMediaUseCase.videos()
            else -> getMediaUseCase.allMedia()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: String) { _filter.value = filter }

    fun deleteMedia(media: Media) {
        viewModelScope.launch {
            try { deleteMediaUseCase(media) } catch (e: Exception) { setError(e.message) }
        }
    }
}
