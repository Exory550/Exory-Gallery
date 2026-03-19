package com.exory550.exorygallery.data.model

data class Album(
    val id: Long,
    val name: String,
    val coverUri: String?,
    val description: String?,
    val mediaCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isLocked: Boolean,
    val sortOrder: Int
)
