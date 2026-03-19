package com.exory550.exorygallery.data.local.database.dao

import androidx.room.*
import com.exory550.exorygallery.data.local.database.entities.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultItemDao {
    @Query("SELECT * FROM media WHERE isVaulted = 1 ORDER BY dateTaken DESC")
    fun getVaultItems(): Flow<List<MediaEntity>>

    @Query("UPDATE media SET isVaulted = 1, vaultPath = :vaultPath WHERE id = :id")
    suspend fun moveToVault(id: Long, vaultPath: String)

    @Query("UPDATE media SET isVaulted = 0, vaultPath = NULL WHERE id = :id")
    suspend fun removeFromVault(id: Long)

    @Query("SELECT COUNT(*) FROM media WHERE isVaulted = 1")
    suspend fun getVaultCount(): Int
}
