package com.exory550.exorygallery.domain.usecases.album

import com.exory550.exorygallery.data.repository.AlbumRepository
import javax.inject.Inject

class CreateAlbumUseCase @Inject constructor(
    private val albumRepository: AlbumRepository
) {
    suspend operator fun invoke(name: String, description: String? = null): Long =
        albumRepository.createAlbum(name, description)
}
