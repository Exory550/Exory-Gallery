package com.exory550.exorygallery.presentation.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.screens.albums.AlbumsScreen
import com.exory550.exorygallery.presentation.screens.explore.ExploreScreen
import com.exory550.exorygallery.presentation.screens.home.HomeScreen
import com.exory550.exorygallery.presentation.screens.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Filled.PhotoLibrary else Icons.Default.PhotoLibrary, null) },
                    label = { Text("Foto") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Collections, null) },
                    label = { Text("Album") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Map, null) },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profil") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeScreen(navController)
                1 -> AlbumsScreen(navController)
                2 -> ExploreScreen(navController)
                3 -> ProfileScreen(navController)
            }
        }
    }
}
