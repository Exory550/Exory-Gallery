package com.exory550.exorygallery.data.local.database.dao

import androidx.room.*
import com.exory550.exorygallery.data.local.database.entities.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE mediaId = :mediaId")
    fun getTagsForMedia(mediaId: Long): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE mediaId = :mediaId")
    suspend fun deleteTagsForMedia(mediaId: Long)
}
