package com.exory550.exorygallery.data.datasource

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.exory550.exorygallery.data.local.database.entities.MediaEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMediaDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver

    fun scanImages(): List<MediaEntity> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        return queryMedia(collection, MediaStore.Images.Media.MIME_TYPE)
    }

    fun scanVideos(): List<MediaEntity> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        return queryMedia(collection, MediaStore.Video.Media.MIME_TYPE)
    }

    private fun queryMedia(uri: Uri, mimeTypeColumn: String): List<MediaEntity> {
        val result = mutableListOf<MediaEntity>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            mimeTypeColumn,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        val cursor = contentResolver.query(uri, projection, null, null, "${MediaStore.MediaColumns.DATE_TAKEN} DESC")
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(mimeTypeColumn)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val widthCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val heightCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
            val modCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(uri, id).toString()
                result.add(MediaEntity(
                    uri = contentUri,
                    path = it.getString(dataCol) ?: "",
                    name = it.getString(nameCol) ?: "",
                    mimeType = it.getString(mimeCol) ?: "",
                    size = it.getLong(sizeCol),
                    width = it.getInt(widthCol),
                    height = it.getInt(heightCol),
                    duration = null,
                    dateTaken = it.getLong(dateCol),
                    dateModified = it.getLong(modCol),
                    albumId = null,
                    latitude = null,
                    longitude = null
                ))
            }
        }
        return result
    }
}
