package com.exory550.exorygallery.data.local.database.dao

import androidx.room.*
import com.exory550.exorygallery.data.local.database.entities.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media ORDER BY dateTaken DESC")
    fun getAllMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE albumId = :albumId ORDER BY dateTaken DESC")
    fun getMediaByAlbum(albumId: Long): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE id = :id")
    suspend fun getMediaById(id: Long): MediaEntity?

    @Query("SELECT * FROM media WHERE mimeType LIKE 'image/%' ORDER BY dateTaken DESC")
    fun getAllImages(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE mimeType LIKE 'video/%' ORDER BY dateTaken DESC")
    fun getAllVideos(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE isVaulted = 1")
    fun getVaultedMedia(): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaList: List<MediaEntity>)

    @Update
    suspend fun updateMedia(media: MediaEntity)

    @Delete
    suspend fun deleteMedia(media: MediaEntity)

    @Query("DELETE FROM media WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM media")
    suspend fun getMediaCount(): Int

    @Query("SELECT SUM(size) FROM media")
    suspend fun getTotalSize(): Long
}
