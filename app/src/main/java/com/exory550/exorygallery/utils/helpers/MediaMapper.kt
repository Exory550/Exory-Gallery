package com.exory550.exorygallery.utils.helpers

import com.exory550.exorygallery.data.local.database.entities.MediaEntity
import com.exory550.exorygallery.data.model.Media

object MediaMapper {
    fun toDomain(e: MediaEntity) = Media(
        id = e.id, uri = e.uri, path = e.path, name = e.name,
        mimeType = e.mimeType, size = e.size, width = e.width,
        height = e.height, duration = e.duration, dateTaken = e.dateTaken,
        dateModified = e.dateModified, albumId = e.albumId,
        latitude = e.latitude, longitude = e.longitude,
        isVaulted = e.isVaulted, isFavorite = e.isFavorite, hash = e.hash
    )

    fun toEntity(m: Media) = MediaEntity(
        id = m.id, uri = m.uri, path = m.path, name = m.name,
        mimeType = m.mimeType, size = m.size, width = m.width,
        height = m.height, duration = m.duration, dateTaken = m.dateTaken,
        dateModified = m.dateModified, albumId = m.albumId,
        latitude = m.latitude, longitude = m.longitude,
        isVaulted = m.isVaulted, isFavorite = m.isFavorite, hash = m.hash
    )
}
