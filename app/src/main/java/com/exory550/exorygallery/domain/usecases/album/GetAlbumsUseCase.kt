package com.exory550.exorygallery.domain.usecases.album

import com.exory550.exorygallery.data.model.Album
import com.exory550.exorygallery.data.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val albumRepository: AlbumRepository
) {
    operator fun invoke(): Flow<List<Album>> = albumRepository.getAllAlbums()
    suspend fun byId(id: Long): Album? = albumRepository.getAlbumById(id)
}
