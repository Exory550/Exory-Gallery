package com.exory550.exorygallery.data.repository

import com.exory550.exorygallery.data.datasource.FileDataSource
import com.exory550.exorygallery.data.local.database.dao.VaultItemDao
import com.exory550.exorygallery.data.local.prefs.UserPreferences
import com.exory550.exorygallery.data.model.Media
import com.exory550.exorygallery.utils.helpers.EncryptionHelper
import com.exory550.exorygallery.utils.helpers.MediaMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepository @Inject constructor(
    private val vaultItemDao: VaultItemDao,
    private val fileDataSource: FileDataSource,
    private val encryptionHelper: EncryptionHelper,
    private val userPreferences: UserPreferences
) {
    fun getVaultItems(): Flow<List<Media>> =
        vaultItemDao.getVaultItems().map { list -> list.map { MediaMapper.toDomain(it) } }

    suspend fun moveToVault(media: Media, pin: String) {
        val sourceFile = File(media.path)
        val encryptedName = "${media.id}.enc"
        val encryptedFile = fileDataSource.getVaultFile(encryptedName)
        encryptionHelper.encryptFile(sourceFile, encryptedFile, pin)
        vaultItemDao.moveToVault(media.id, encryptedFile.absolutePath)
    }

    suspend fun removeFromVault(media: Media, pin: String) {
        val encryptedFile = File(media.path)
        val destFile = fileDataSource.createTempFile("dec_", ".tmp")
        encryptionHelper.decryptFile(encryptedFile, destFile, pin)
        vaultItemDao.removeFromVault(media.id)
        fileDataSource.deleteVaultFile("${media.id}.enc")
    }

    suspend fun getVaultCount(): Int = vaultItemDao.getVaultCount()

    suspend fun getVaultHash(): String? = userPreferences.vaultHash.first()

    suspend fun setVaultPin(pin: String) {
        val hash = encryptionHelper.hashPin(pin)
        userPreferences.setVaultHash(hash)
    }

    suspend fun verifyPin(pin: String): Boolean {
        val hash = userPreferences.vaultHash.first()
        return hash != null && encryptionHelper.verifyPin(pin, hash)
    }
}
