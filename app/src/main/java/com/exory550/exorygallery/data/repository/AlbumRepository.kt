package com.exory550.exorygallery.data.repository

import com.exory550.exorygallery.data.local.database.dao.AlbumDao
import com.exory550.exorygallery.data.local.database.entities.AlbumEntity
import com.exory550.exorygallery.data.model.Album
import com.exory550.exorygallery.utils.helpers.AlbumMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumRepository @Inject constructor(
    private val albumDao: AlbumDao
) {
    fun getAllAlbums(): Flow<List<Album>> =
        albumDao.getAllAlbums().map { list -> list.map { AlbumMapper.toDomain(it) } }

    suspend fun getAlbumById(id: Long): Album? =
        albumDao.getAlbumById(id)?.let { AlbumMapper.toDomain(it) }

    suspend fun createAlbum(name: String, description: String? = null): Long {
        val now = System.currentTimeMillis()
        val entity = AlbumEntity(
            name = name,
            coverUri = null,
            description = description,
            createdAt = now,
            updatedAt = now
        )
        return albumDao.insertAlbum(entity)
    }

    suspend fun updateAlbum(album: Album) {
        albumDao.updateAlbum(AlbumMapper.toEntity(album))
    }

    suspend fun deleteAlbum(album: Album) {
        albumDao.deleteAlbum(AlbumMapper.toEntity(album))
    }

    suspend fun getMediaCountForAlbum(albumId: Long): Int =
        albumDao.getMediaCountForAlbum(albumId)
}
