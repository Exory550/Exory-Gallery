package com.exory550.exorygallery.domain.usecases.vault

import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.data.repository.VaultRepository
import javax.inject.Inject

class EncryptMediaUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(media: Media, pin: String) =
        vaultRepository.moveToVault(media, pin)
}
