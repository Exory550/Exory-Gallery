package com.exory550.exorygallery.domain.model

data class Metadata(
    val make: String?,
    val model: String?,
    val aperture: String?,
    val shutterSpeed: String?,
    val iso: String?,
    val focalLength: String?,
    val flash: String?,
    val orientation: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val altitude: Double?,
    val dateTaken: String?,
    val width: Int?,
    val height: Int?,
    val colorSpace: String?,
    val software: String?,
    val address: String?
)
