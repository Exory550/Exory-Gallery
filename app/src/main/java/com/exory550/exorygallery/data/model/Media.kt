package com.exory550.exorygallery.data.model

data class Media(
    val id: Long,
    val uri: String,
    val path: String,
    val name: String,
    val mimeType: String,
    val size: Long,
    val width: Int,
    val height: Int,
    val duration: Long?,
    val dateTaken: Long,
    val dateModified: Long,
    val albumId: Long?,
    val latitude: Double?,
    val longitude: Double?,
    val isVaulted: Boolean,
    val isFavorite: Boolean,
    val hash: String?
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isVideo: Boolean get() = mimeType.startsWith("video/")
    val hasLocation: Boolean get() = latitude != null && longitude != null
}
