package com.exory550.exorygallery.domain.usecases.cleanup

import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.data.repository.GalleryRepository
import com.exory550.exorygallery.utils.constants.AppConstants
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class FindLargeFilesUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository
) {
    suspend operator fun invoke(thresholdBytes: Long = AppConstants.LARGE_FILE_THRESHOLD): List<Media> {
        return galleryRepository.getAllMedia().first()
            .filter { it.size >= thresholdBytes }
            .sortedByDescending { it.size }
    }
}
