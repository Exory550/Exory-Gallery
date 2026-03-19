package com.exory550.exorygallery.domain.usecases.cleanup

import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.data.repository.GalleryRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FindOldMediaUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository
) {
    suspend operator fun invoke(years: Int = 2): List<Media> {
        val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(years * 365L)
        return galleryRepository.getAllMedia().first()
            .filter { it.dateTaken < threshold }
            .sortedBy { it.dateTaken }
    }
}
