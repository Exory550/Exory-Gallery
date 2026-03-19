package com.exory550.exorygallery.utils.extensions

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

fun Bitmap.saveToFile(file: File, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 90): Boolean {
    return try {
        FileOutputStream(file).use { compress(format, quality, it) }
        true
    } catch (e: Exception) {
        false
    }
}

fun Bitmap.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 90): ByteArray {
    val stream = java.io.ByteArrayOutputStream()
    compress(format, quality, stream)
    return stream.toByteArray()
}
