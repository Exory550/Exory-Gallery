package com.exory550.exorygallery.presentation.screens.main

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exory550.exorygallery.presentation.screens.albums.AlbumsScreen
import com.exory550.exorygallery.presentation.screens.explore.ExploreScreen
import com.exory550.exorygallery.presentation.screens.home.HomeScreen
import com.exory550.exorygallery.presentation.screens.profile.ProfileScreen

sealed class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Photos : BottomTab("tab_photos", "Foto", Icons.Filled.Photo, Icons.Outlined.Photo)
    object Albums : BottomTab("tab_albums", "Album", Icons.Filled.PhotoAlbum, Icons.Outlined.PhotoAlbum)
    object Explore : BottomTab("tab_explore", "Explore", Icons.Filled.Explore, Icons.Outlined.Explore)
    object Profile : BottomTab("tab_profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val tabs = listOf(BottomTab.Photos, BottomTab.Albums, BottomTab.Explore, BottomTab.Profile)
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
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
