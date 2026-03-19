package com.exory550.exorygallery.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val coverUri: String?,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isLocked: Boolean = false,
    val sortOrder: Int = 0
)
