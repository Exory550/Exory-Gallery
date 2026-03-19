package com.exory550.exorygallery.di

import com.exory550.exorygallery.data.datasource.FileDataSource
import com.exory550.exorygallery.data.datasource.LocalMediaDataSource
import com.exory550.exorygallery.data.local.database.dao.AlbumDao
import com.exory550.exorygallery.data.local.database.dao.MediaDao
import com.exory550.exorygallery.data.local.database.dao.VaultItemDao
import com.exory550.exorygallery.data.local.prefs.UserPreferences
import com.exory550.exorygallery.data.repository.AlbumRepository
import com.exory550.exorygallery.data.repository.GalleryRepository
import com.exory550.exorygallery.data.repository.VaultRepository
import com.exory550.exorygallery.utils.helpers.EncryptionHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideGalleryRepository(
        mediaDao: MediaDao,
        localMediaDataSource: LocalMediaDataSource
    ): GalleryRepository = GalleryRepository(mediaDao, localMediaDataSource)

    @Provides
    @Singleton
    fun provideAlbumRepository(
        albumDao: AlbumDao
    ): AlbumRepository = AlbumRepository(albumDao)

    @Provides
    @Singleton
    fun provideVaultRepository(
        vaultItemDao: VaultItemDao,
        fileDataSource: FileDataSource,
        encryptionHelper: EncryptionHelper,
        userPreferences: UserPreferences
    ): VaultRepository = VaultRepository(
        vaultItemDao,
        fileDataSource,
        encryptionHelper,
        userPreferences
    )
}
