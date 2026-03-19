package com.exory550.exorygallery.domain.usecases.album

import com.exory550.exorygallery.data.model.Album
import com.exory550.exorygallery.data.repository.AlbumRepository
import javax.inject.Inject

class UpdateAlbumUseCase @Inject constructor(
    private val albumRepository: AlbumRepository
) {
    suspend operator fun invoke(album: Album) = albumRepository.updateAlbum(album)
}
