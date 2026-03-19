package com.exory550.exorygallery.data.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vaultDir: File get() {
        val dir = File(context.filesDir, "vault")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getVaultFile(name: String): File = File(vaultDir, name)

    fun copyToVault(source: File, destName: String): File {
        val dest = getVaultFile(destName)
        source.copyTo(dest, overwrite = true)
        return dest
    }

    fun deleteVaultFile(name: String): Boolean {
        return getVaultFile(name).delete()
    }

    fun listVaultFiles(): List<File> = vaultDir.listFiles()?.toList() ?: emptyList()

    fun openInputStream(file: File): InputStream = file.inputStream()

    fun openOutputStream(file: File): OutputStream = file.outputStream()

    fun getTempDir(): File {
        val dir = File(context.cacheDir, "temp")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createTempFile(prefix: String, suffix: String): File =
        File.createTempFile(prefix, suffix, getTempDir())

    fun clearTempDir() {
        getTempDir().listFiles()?.forEach { it.delete() }
    }
}
