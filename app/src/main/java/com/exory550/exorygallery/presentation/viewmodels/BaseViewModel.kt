package com.exory550.exorygallery.presentation.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    protected fun setLoading(value: Boolean) { _isLoading.value = value }
    protected fun setError(msg: String?) { _error.value = msg }
    fun clearError() { _error.value = null }
}
