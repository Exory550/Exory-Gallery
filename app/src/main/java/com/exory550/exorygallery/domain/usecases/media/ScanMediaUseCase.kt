package com.exory550.exorygallery.domain.usecases.media

import com.exory550.exorygallery.data.repository.GalleryRepository
import javax.inject.Inject

class ScanMediaUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository
) {
    suspend operator fun invoke() = galleryRepository.scanAndSync()
}
