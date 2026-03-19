package com.exory550.exorygallery.domain.usecases.cleanup

import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.data.repository.GalleryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class FindDuplicatesUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository
) {
    suspend operator fun invoke(): List<List<Media>> {
        val all = galleryRepository.getAllMedia().first()
        return all.filter { it.hash != null }
            .groupBy { it.hash }
            .values
            .filter { it.size > 1 }
    }
}
