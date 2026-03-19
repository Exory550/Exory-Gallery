package com.exory550.exorygallery.utils.extensions

import java.io.File

fun File.isImage(): Boolean = extension.lowercase() in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

fun File.isVideo(): Boolean = extension.lowercase() in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm")

fun File.sizeInMb(): Double = length() / (1024.0 * 1024.0)
