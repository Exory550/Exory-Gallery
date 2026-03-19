package com.exory550.exorygallery.utils.helpers

import androidx.exifinterface.media.ExifInterface
import com.exory550.exorygallery.domain.model.Metadata
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataHelper @Inject constructor() {
    fun extract(file: File): Metadata {
        val exif = ExifInterface(file.absolutePath)
        val latLong = FloatArray(2)
        val hasLocation = exif.getLatLong(latLong)
        return Metadata(
            make = exif.getAttribute(ExifInterface.TAG_MAKE),
            model = exif.getAttribute(ExifInterface.TAG_MODEL),
            aperture = exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE),
            shutterSpeed = exif.getAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE),
            iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS),
            focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH),
            flash = exif.getAttribute(ExifInterface.TAG_FLASH),
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL),
            latitude = if (hasLocation) latLong[0].toDouble() else null,
            longitude = if (hasLocation) latLong[1].toDouble() else null,
            altitude = exif.getAltitude(0.0),
            dateTaken = exif.getAttribute(ExifInterface.TAG_DATETIME),
            width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0),
            height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0),
            colorSpace = exif.getAttribute(ExifInterface.TAG_COLOR_SPACE),
            software = exif.getAttribute(ExifInterface.TAG_SOFTWARE),
            address = null
        )
    }

    fun strip(file: File): Boolean {
        return try {
            val exif = ExifInterface(file.absolutePath)
            listOf(
                ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_ALTITUDE, ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL, ExifInterface.TAG_SOFTWARE
            ).forEach { exif.setAttribute(it, null) }
            exif.saveAttributes()
            true
        } catch (e: Exception) { false }
    }
}
