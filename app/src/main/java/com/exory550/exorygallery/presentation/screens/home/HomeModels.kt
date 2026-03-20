package com.exory550.exorygallery.presentation.screens.home

data class MediaFolder(
    val name: String,
    val path: String,
    val coverUri: String,
    val count: Int,
    val hasVideo: Boolean = false
)

data class TimelineGroup(val label: String, val photos: List<String>)

enum class ViewMode { GRID_2, GRID_3, GRID_4, TIMELINE }
