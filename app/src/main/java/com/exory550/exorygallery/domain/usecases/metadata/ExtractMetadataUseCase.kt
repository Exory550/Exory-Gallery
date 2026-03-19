package com.exory550.exorygallery.domain.usecases.metadata

import com.exory550.exorygallery.domain.model.Metadata
import com.exory550.exorygallery.utils.helpers.MetadataHelper
import java.io.File
import javax.inject.Inject

class ExtractMetadataUseCase @Inject constructor(
    private val metadataHelper: MetadataHelper
) {
    operator fun invoke(file: File): Metadata = metadataHelper.extract(file)
}
