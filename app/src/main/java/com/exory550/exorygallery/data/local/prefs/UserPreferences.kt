package com.exory550.exorygallery.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.exory550.exorygallery.utils.constants.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = AppConstants.PREFS_NAME)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val VAULT_HASH = stringPreferencesKey("vault_hash")
        val VAULT_ENABLED = booleanPreferencesKey("vault_enabled")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SHOW_HIDDEN = booleanPreferencesKey("show_hidden")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
    }

    val themeMode: Flow<String> = context.dataStore.data.catch { emit(emptyPreferences()) }
        .map { it[Keys.THEME_MODE] ?: "system" }

    val gridColumns: Flow<Int> = context.dataStore.data.catch { emit(emptyPreferences()) }
        .map { it[Keys.GRID_COLUMNS] ?: 3 }

    val vaultEnabled: Flow<Boolean> = context.dataStore.data.catch { emit(emptyPreferences()) }
        .map { it[Keys.VAULT_ENABLED] ?: false }

    val vaultHash: Flow<String?> = context.dataStore.data.catch { emit(emptyPreferences()) }
        .map { it[Keys.VAULT_HASH] }

    val sortOrder: Flow<String> = context.dataStore.data.catch { emit(emptyPreferences()) }
        .map { it[Keys.SORT_ORDER] ?: "date_desc" }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setGridColumns(columns: Int) {
        context.dataStore.edit { it[Keys.GRID_COLUMNS] = columns }
    }

    suspend fun setVaultHash(hash: String) {
        context.dataStore.edit {
            it[Keys.VAULT_HASH] = hash
            it[Keys.VAULT_ENABLED] = true
        }
    }

    suspend fun setSortOrder(order: String) {
        context.dataStore.edit { it[Keys.SORT_ORDER] = order }
    }

    suspend fun clearVault() {
        context.dataStore.edit {
            it.remove(Keys.VAULT_HASH)
            it[Keys.VAULT_ENABLED] = false
        }
    }
}
