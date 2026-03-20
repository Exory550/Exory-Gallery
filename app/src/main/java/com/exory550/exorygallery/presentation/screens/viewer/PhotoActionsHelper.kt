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

object PhotoActionsHelper {

    suspend fun deleteFile(context: Context, path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val uri = getMediaUri(context.contentResolver, path)
                    if (uri != null) {
                        context.contentResolver.delete(uri, null, null) > 0
                    } else {
                        file.delete()
                    }
                } else {
                    file.delete()
                }
            } catch (e: Exception) { false }
        }
    }

    suspend fun copyFile(sourcePath: String, destFolder: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val source = File(sourcePath)
                val destDir = File(destFolder)
                if (!destDir.exists()) destDir.mkdirs()
                val dest = File(destDir, source.name)
                source.copyTo(dest, overwrite = false)
                dest.absolutePath
            } catch (e: Exception) { null }
        }
    }

    suspend fun moveFile(sourcePath: String, destFolder: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val source = File(sourcePath)
                val destDir = File(destFolder)
                if (!destDir.exists()) destDir.mkdirs()
                val dest = File(destDir, source.name)
                source.copyTo(dest, overwrite = false)
                source.delete()
                dest.absolutePath
            } catch (e: Exception) { null }
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
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (path.endsWith(".mp4") || path.endsWith(".mkv")) "video/*" else "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan"))
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
            context.startActivity(Intent.createChooser(intent, "Buka dengan"))
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
            context.startActivity(Intent.createChooser(intent, "Atur sebagai"))
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
