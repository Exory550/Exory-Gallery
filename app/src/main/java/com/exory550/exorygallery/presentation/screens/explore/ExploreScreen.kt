package com.exory550.exorygallery.presentation.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.screens.map.MapScreen

@Composable
fun ExploreScreen(navController: NavController) {
    MapScreen(navController)
}
