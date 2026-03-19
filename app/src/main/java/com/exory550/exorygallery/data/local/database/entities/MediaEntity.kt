package com.exory550.exorygallery.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media",
    foreignKeys = [ForeignKey(
        entity = AlbumEntity::class,
        parentColumns = ["id"],
        childColumns = ["albumId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("albumId")]
)
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val isVaulted: Boolean = false,
    val vaultPath: String? = null,
    val isFavorite: Boolean = false,
    val hash: String? = null
)
