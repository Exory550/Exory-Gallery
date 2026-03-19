package com.exory550.exorygallery.di

import android.content.Context
import androidx.room.Room
import com.exory550.exorygallery.data.local.database.GalleryDatabase
import com.exory550.exorygallery.data.local.database.dao.AlbumDao
import com.exory550.exorygallery.data.local.database.dao.MediaDao
import com.exory550.exorygallery.data.local.database.dao.TagDao
import com.exory550.exorygallery.data.local.database.dao.VaultItemDao
import com.exory550.exorygallery.utils.constants.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GalleryDatabase =
        Room.databaseBuilder(context, GalleryDatabase::class.java, AppConstants.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMediaDao(db: GalleryDatabase): MediaDao = db.mediaDao()
    @Provides fun provideAlbumDao(db: GalleryDatabase): AlbumDao = db.albumDao()
    @Provides fun provideTagDao(db: GalleryDatabase): TagDao = db.tagDao()
    @Provides fun provideVaultItemDao(db: GalleryDatabase): VaultItemDao = db.vaultItemDao()
}
