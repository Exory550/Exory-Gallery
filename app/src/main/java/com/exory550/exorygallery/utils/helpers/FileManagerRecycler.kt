package com.exory550.exorygallery.utils.helpers

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileManagerRecycler @Inject constructor() {
    fun deleteFiles(files: List<File>): Int {
        var count = 0
        files.forEach { if (it.exists() && it.delete()) count++ }
        return count
    }

    fun getTotalSize(files: List<File>): Long = files.sumOf { it.length() }

    fun filterByExtension(files: List<File>, extensions: List<String>): List<File> =
        files.filter { it.extension.lowercase() in extensions }
}
