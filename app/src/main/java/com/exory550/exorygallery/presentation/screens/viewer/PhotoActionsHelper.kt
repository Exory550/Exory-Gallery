package com.exory550.exorygallery.presentation.screens.viewer

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class ConflictAction { SKIP, OVERWRITE, KEEP_BOTH }

data class CopyResult(
    val success: Boolean,
    val conflict: Boolean = false,
    val conflictFileName: String = "",
    val destPath: String = ""
)

object PhotoActionsHelper {

    suspend fun copyFile(sourcePath: String, destFolder: String, conflictAction: ConflictAction = ConflictAction.KEEP_BOTH): CopyResult {
        return withContext(Dispatchers.IO) {
            try {
                val source = File(sourcePath)
                val destDir = File(destFolder)
                if (!destDir.exists()) destDir.mkdirs()
                val destFile = File(destDir, source.name)

                if (destFile.exists()) {
                    return@withContext CopyResult(false, conflict = true, conflictFileName = source.name, destPath = destFile.absolutePath)
                }

                source.copyTo(destFile, overwrite = false)
                CopyResult(true, destPath = destFile.absolutePath)
            } catch (e: Exception) {
                CopyResult(false)
            }
        }
    }

    suspend fun copyFileWithAction(sourcePath: String, destFolder: String, action: ConflictAction): CopyResult {
        return withContext(Dispatchers.IO) {
            try {
                val source = File(sourcePath)
                val destDir = File(destFolder)
                if (!destDir.exists()) destDir.mkdirs()

                val destFile = when (action) {
                    ConflictAction.SKIP -> return@withContext CopyResult(true)
                    ConflictAction.OVERWRITE -> File(destDir, source.name)
                    ConflictAction.KEEP_BOTH -> {
                        var counter = 1
                        var newFile = File(destDir, "${source.nameWithoutExtension}_$counter.${source.extension}")
                        while (newFile.exists()) { counter++; newFile = File(destDir, "${source.nameWithoutExtension}_$counter.${source.extension}") }
                        newFile
                    }
                }
                source.copyTo(destFile, overwrite = action == ConflictAction.OVERWRITE)
                CopyResult(true, destPath = destFile.absolutePath)
            } catch (e: Exception) {
                CopyResult(false)
            }
        }
    }

    suspend fun moveFile(sourcePath: String, destFolder: String, conflictAction: ConflictAction = ConflictAction.KEEP_BOTH): CopyResult {
        return withContext(Dispatchers.IO) {
            try {
                val source = File(sourcePath)
                val destDir = File(destFolder)
                if (!destDir.exists()) destDir.mkdirs()
                val destFile = File(destDir, source.name)

                if (destFile.exists()) {
                    return@withContext CopyResult(false, conflict = true, conflictFileName = source.name, destPath = destFile.absolutePath)
                }

                source.copyTo(destFile, overwrite = false)
                source.delete()
                CopyResult(true, destPath = destFile.absolutePath)
            } catch (e: Exception) {
                CopyResult(false)
            }
        }
    }

    suspend fun moveFileWithAction(sourcePath: String, destFolder: String, action: ConflictAction): CopyResult {
        return withContext(Dispatchers.IO) {
            try {
                val source = File(sourcePath)
                val destDir = File(destFolder)
                if (!destDir.exists()) destDir.mkdirs()

                val destFile = when (action) {
                    ConflictAction.SKIP -> return@withContext CopyResult(true)
                    ConflictAction.OVERWRITE -> File(destDir, source.name)
                    ConflictAction.KEEP_BOTH -> {
                        var counter = 1
                        var newFile = File(destDir, "${source.nameWithoutExtension}_$counter.${source.extension}")
                        while (newFile.exists()) { counter++; newFile = File(destDir, "${source.nameWithoutExtension}_$counter.${source.extension}") }
                        newFile
                    }
                }
                source.copyTo(destFile, overwrite = action == ConflictAction.OVERWRITE)
                source.delete()
                CopyResult(true, destPath = destFile.absolutePath)
            } catch (e: Exception) {
                CopyResult(false)
            }
        }
    }

    suspend fun deleteFile(context: Context, path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val uri = getMediaUri(context.contentResolver, path)
                    if (uri != null) context.contentResolver.delete(uri, null, null) > 0
                    else file.delete()
                } else file.delete()
            } catch (e: Exception) { false }
        }
    }

    suspend fun moveToTrash(context: Context, path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                val trashDir = File(context.filesDir, ".trash")
                if (!trashDir.exists()) trashDir.mkdirs()
                val trashFile = File(trashDir, "${System.currentTimeMillis()}_${file.name}")
                file.copyTo(trashFile)
                file.delete()
                true
            } catch (e: Exception) { false }
        }
    }

    suspend fun hideFile(path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                val hiddenFile = File(file.parent, ".${file.name}")
                file.renameTo(hiddenFile)
                val nomedia = File(file.parent, ".nomedia")
                if (!nomedia.exists()) nomedia.createNewFile()
                true
            } catch (e: Exception) { false }
        }
    }

    suspend fun renameFile(path: String, newName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                val newFile = File(file.parent, "$newName.${file.extension}")
                file.renameTo(newFile)
                newFile.absolutePath
            } catch (e: Exception) { null }
        }
    }

    fun shareFile(context: Context, path: String) {
        try {
            val file = File(path)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val mimeType = when (file.extension.lowercase()) {
                "mp4", "mkv", "avi", "mov" -> "video/*"
                "png" -> "image/png"
                "gif" -> "image/gif"
                else -> "image/jpeg"
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun openWith(context: Context, path: String) {
        try {
            val file = File(path)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Buka dengan").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun setAsWallpaper(context: Context, path: String) {
        try {
            val file = File(path)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                setDataAndType(uri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("mimeType", "image/*")
            }
            context.startActivity(Intent.createChooser(intent, "Atur sebagai").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun getMediaUri(cr: ContentResolver, path: String): Uri? {
        val cursor = cr.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            "${MediaStore.Images.Media.DATA} = ?",
            arrayOf(path), null
        )
        return cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
            } else null
        }
    }

    fun getFileProperties(path: String): Map<String, String> {
        val file = File(path)
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        return mapOf(
            "Nama" to file.name,
            "Lokasi" to (file.parent ?: "-"),
            "Ukuran" to "%.2f MB".format(file.length() / (1024.0 * 1024.0)),
            "Diubah" to sdf.format(java.util.Date(file.lastModified())),
            "Ekstensi" to file.extension.uppercase()
        )
    }
}
