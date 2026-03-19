package com.exory550.exorygallery.di

import com.exory550.exorygallery.data.local.database.dao.MediaDao
import com.exory550.exorygallery.data.repository.AlbumRepository
import com.exory550.exorygallery.data.repository.GalleryRepository
import com.exory550.exorygallery.data.repository.VaultRepository
import com.exory550.exorygallery.domain.usecases.album.CreateAlbumUseCase
import com.exory550.exorygallery.domain.usecases.album.GetAlbumsUseCase
import com.exory550.exorygallery.domain.usecases.album.UpdateAlbumUseCase
import com.exory550.exorygallery.domain.usecases.cleanup.FindDuplicatesUseCase
import com.exory550.exorygallery.domain.usecases.cleanup.FindLargeFilesUseCase
import com.exory550.exorygallery.domain.usecases.cleanup.FindOldMediaUseCase
import com.exory550.exorygallery.domain.usecases.media.DeleteMediaUseCase
import com.exory550.exorygallery.domain.usecases.media.GetMediaUseCase
import com.exory550.exorygallery.domain.usecases.media.MoveMediaUseCase
import com.exory550.exorygallery.domain.usecases.media.ScanMediaUseCase
import com.exory550.exorygallery.domain.usecases.vault.DecryptMediaUseCase
import com.exory550.exorygallery.domain.usecases.vault.EncryptMediaUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides fun provideGetMediaUseCase(r: GalleryRepository) = GetMediaUseCase(r)
    @Provides fun provideScanMediaUseCase(r: GalleryRepository) = ScanMediaUseCase(r)
    @Provides fun provideDeleteMediaUseCase(r: GalleryRepository) = DeleteMediaUseCase(r)
    @Provides fun provideMoveMediaUseCase(r: GalleryRepository, d: MediaDao) = MoveMediaUseCase(r, d)
    @Provides fun provideGetAlbumsUseCase(r: AlbumRepository) = GetAlbumsUseCase(r)
    @Provides fun provideCreateAlbumUseCase(r: AlbumRepository) = CreateAlbumUseCase(r)
    @Provides fun provideUpdateAlbumUseCase(r: AlbumRepository) = UpdateAlbumUseCase(r)
    @Provides fun provideFindDuplicatesUseCase(r: GalleryRepository) = FindDuplicatesUseCase(r)
    @Provides fun provideFindLargeFilesUseCase(r: GalleryRepository) = FindLargeFilesUseCase(r)
    @Provides fun provideFindOldMediaUseCase(r: GalleryRepository) = FindOldMediaUseCase(r)
    @Provides fun provideEncryptMediaUseCase(r: VaultRepository) = EncryptMediaUseCase(r)
    @Provides fun provideDecryptMediaUseCase(r: VaultRepository) = DecryptMediaUseCase(r)
}
