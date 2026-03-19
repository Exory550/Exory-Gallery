package com.exory550.exorygallery.domain.usecases.vault

import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.data.repository.VaultRepository
import javax.inject.Inject

class DecryptMediaUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(media: Media, pin: String) =
        vaultRepository.removeFromVault(media, pin)
}
