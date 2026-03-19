package com.exory550.exorygallery.domain.usecases.media

import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.data.repository.GalleryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMediaUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository
) {
    fun allMedia(): Flow<List<Media>> = galleryRepository.getAllMedia()
    fun images(): Flow<List<Media>> = galleryRepository.getAllImages()
    fun videos(): Flow<List<Media>> = galleryRepository.getAllVideos()
    fun byAlbum(albumId: Long): Flow<List<Media>> = galleryRepository.getMediaByAlbum(albumId)
    suspend fun byId(id: Long): Media? = galleryRepository.getMediaById(id)
}
