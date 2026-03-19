package com.exory550.exorygallery.data.repository

import com.exory550.exorygallery.data.datasource.LocalMediaDataSource
import com.exory550.exorygallery.data.local.database.dao.MediaDao
import com.exory550.exorygallery.data.local.database.entities.MediaEntity
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.utils.helpers.MediaMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    private val mediaDao: MediaDao,
    private val localMediaDataSource: LocalMediaDataSource
) {
    fun getAllMedia(): Flow<List<Media>> =
        mediaDao.getAllMedia().map { list -> list.map { MediaMapper.toDomain(it) } }

    fun getAllImages(): Flow<List<Media>> =
        mediaDao.getAllImages().map { list -> list.map { MediaMapper.toDomain(it) } }

    fun getAllVideos(): Flow<List<Media>> =
        mediaDao.getAllVideos().map { list -> list.map { MediaMapper.toDomain(it) } }

    fun getMediaByAlbum(albumId: Long): Flow<List<Media>> =
        mediaDao.getMediaByAlbum(albumId).map { list -> list.map { MediaMapper.toDomain(it) } }

    suspend fun getMediaById(id: Long): Media? =
        mediaDao.getMediaById(id)?.let { MediaMapper.toDomain(it) }

    suspend fun scanAndSync() {
        val images = localMediaDataSource.scanImages()
        val videos = localMediaDataSource.scanVideos()
        mediaDao.insertAll(images + videos)
    }

    suspend fun deleteMedia(media: Media) = mediaDao.deleteById(media.id)

    suspend fun updateMedia(entity: MediaEntity) = mediaDao.updateMedia(entity)

    suspend fun getMediaCount(): Int = mediaDao.getMediaCount()

    suspend fun getTotalSize(): Long = mediaDao.getTotalSize()
}
