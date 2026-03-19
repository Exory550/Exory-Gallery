package com.exory550.exorygallery.presentation.screens.media

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.domain.model.Metadata
import com.exory550.exorygallery.domain.usecases.media.DeleteMediaUseCase
import com.exory550.exorygallery.domain.usecases.media.GetMediaUseCase
import com.exory550.exorygallery.domain.usecases.metadata.ExtractMetadataUseCase
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val getMediaUseCase: GetMediaUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase,
    private val extractMetadataUseCase: ExtractMetadataUseCase
) : BaseViewModel() {
    private val _media = MutableStateFlow<Media?>(null)
    val media: StateFlow<Media?> = _media

    private val _metadata = MutableStateFlow<Metadata?>(null)
    val metadata: StateFlow<Metadata?> = _metadata

    fun loadMedia(id: Long) {
        viewModelScope.launch {
            _media.value = getMediaUseCase.byId(id)
            _media.value?.let { m ->
                _metadata.value = extractMetadataUseCase(File(m.path))
            }
        }
    }

    fun deleteMedia() {
        viewModelScope.launch {
            _media.value?.let { deleteMediaUseCase(it) }
        }
    }
}
