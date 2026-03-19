package com.exory550.exorygallery.utils.helpers

import com.exory550.exorygallery.data.local.database.entities.AlbumEntity
import com.exory550.exorygallery.data.model.Album

object AlbumMapper {
    fun toDomain(e: AlbumEntity) = Album(
        id = e.id, name = e.name, coverUri = e.coverUri,
        description = e.description, mediaCount = 0,
        createdAt = e.createdAt, updatedAt = e.updatedAt,
        isLocked = e.isLocked, sortOrder = e.sortOrder
    )

    fun toEntity(a: Album) = AlbumEntity(
        id = a.id, name = a.name, coverUri = a.coverUri,
        description = a.description, createdAt = a.createdAt,
        updatedAt = a.updatedAt, isLocked = a.isLocked, sortOrder = a.sortOrder
    )
}
