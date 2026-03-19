package com.exory550.exorygallery.presentation.screens.settings

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.local.prefs.UserPreferences
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : BaseViewModel() {
    val themeMode: StateFlow<String> = userPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val gridColumns: StateFlow<Int> = userPreferences.gridColumns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    fun setTheme(mode: String) = viewModelScope.launch { userPreferences.setThemeMode(mode) }
    fun setGridColumns(cols: Int) = viewModelScope.launch { userPreferences.setGridColumns(cols) }
}
