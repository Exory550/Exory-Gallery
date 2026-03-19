package com.exory550.exorygallery.presentation.screens.albums

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.model.Album
import com.exory550.exorygallery.domain.usecases.album.CreateAlbumUseCase
import com.exory550.exorygallery.domain.usecases.album.GetAlbumsUseCase
import com.exory550.exorygallery.domain.usecases.album.UpdateAlbumUseCase
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val createAlbumUseCase: CreateAlbumUseCase,
    private val updateAlbumUseCase: UpdateAlbumUseCase
) : BaseViewModel() {
    val albums: StateFlow<List<Album>> = getAlbumsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createAlbum(name: String, description: String? = null) {
        viewModelScope.launch {
            try { createAlbumUseCase(name, description) } catch (e: Exception) { setError(e.message) }
        }
    }
}
