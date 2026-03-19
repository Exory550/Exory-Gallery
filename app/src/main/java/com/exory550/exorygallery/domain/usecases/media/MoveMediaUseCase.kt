package com.exory550.exorygallery.domain.usecases.media

import com.exory550.exorygallery.data.local.database.dao.MediaDao
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.data.repository.GalleryRepository
import com.exory550.exorygallery.utils.helpers.MediaMapper
import javax.inject.Inject

class MoveMediaUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val mediaDao: MediaDao
) {
    suspend operator fun invoke(media: Media, targetAlbumId: Long) {
        val entity = MediaMapper.toEntity(media).copy(albumId = targetAlbumId)
        mediaDao.updateMedia(entity)
    }
}
