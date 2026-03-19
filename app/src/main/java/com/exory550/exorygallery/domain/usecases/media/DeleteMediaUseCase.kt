package com.exory550.exorygallery.domain.usecases.media

import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.data.repository.GalleryRepository
import java.io.File
import javax.inject.Inject

class DeleteMediaUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository
) {
    suspend operator fun invoke(media: Media) {
        File(media.path).delete()
        galleryRepository.deleteMedia(media)
    }
}
