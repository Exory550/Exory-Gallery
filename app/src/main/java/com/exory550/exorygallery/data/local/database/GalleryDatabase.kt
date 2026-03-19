package com.exory550.exorygallery.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.exory550.exorygallery.data.local.database.dao.AlbumDao
import com.exory550.exorygallery.data.local.database.dao.MediaDao
import com.exory550.exorygallery.data.local.database.dao.TagDao
import com.exory550.exorygallery.data.local.database.dao.VaultItemDao
import com.exory550.exorygallery.data.local.database.entities.AlbumEntity
import com.exory550.exorygallery.data.local.database.entities.MediaEntity
import com.exory550.exorygallery.data.local.database.entities.TagEntity
import com.exory550.exorygallery.utils.helpers.DateConverters

@Database(
    entities = [MediaEntity::class, AlbumEntity::class, TagEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun albumDao(): AlbumDao
    abstract fun tagDao(): TagDao
    abstract fun vaultItemDao(): VaultItemDao
}
