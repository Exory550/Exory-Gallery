package com.exory550.exorygallery.presentation.screens.vault

import androidx.lifecycle.viewModelScope
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.domain.usecases.vault.DecryptMediaUseCase
import com.exory550.exorygallery.domain.usecases.vault.EncryptMediaUseCase
import com.exory550.exorygallery.data.repository.VaultRepository
import com.exory550.exorygallery.presentation.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val encryptMediaUseCase: EncryptMediaUseCase,
    private val decryptMediaUseCase: DecryptMediaUseCase
) : BaseViewModel() {
    val vaultItems: StateFlow<List<Media>> = vaultRepository.getVaultItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun encrypt(media: Media, pin: String) {
        viewModelScope.launch {
            try { encryptMediaUseCase(media, pin) } catch (e: Exception) { setError(e.message) }
        }
    }

    fun decrypt(media: Media, pin: String) {
        viewModelScope.launch {
            try { decryptMediaUseCase(media, pin) } catch (e: Exception) { setError(e.message) }
        }
    }
}
