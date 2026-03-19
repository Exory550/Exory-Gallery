package com.exory550.exorygallery.presentation.screens.map

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.domain.usecases.media.GetMediaUseCase
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getMediaUseCase: GetMediaUseCase
) : BaseViewModel() {
    val geoMedia: StateFlow<List<Media>> = getMediaUseCase.allMedia()
        .map { list -> list.filter { it.hasLocation } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
