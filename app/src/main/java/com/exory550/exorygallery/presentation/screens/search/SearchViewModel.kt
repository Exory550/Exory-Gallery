package com.exory550.exorygallery.presentation.screens.search

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.domain.usecases.media.GetMediaUseCase
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getMediaUseCase: GetMediaUseCase
) : BaseViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val results: StateFlow<List<Media>> = _query
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList())
            else getMediaUseCase.allMedia().map { list ->
                list.filter { it.name.contains(q, ignoreCase = true) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _query.value = q }
}
