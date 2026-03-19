package com.exory550.exorygallery.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.exory550.exorygallery.presentation.screens.albums.AlbumsScreen
import com.exory550.exorygallery.presentation.screens.gallery.GalleryScreen
import com.exory550.exorygallery.presentation.screens.home.HomeScreen
import com.exory550.exorygallery.presentation.screens.map.MapScreen
import com.exory550.exorygallery.presentation.screens.media.MediaViewerScreen
import com.exory550.exorygallery.presentation.screens.search.SearchScreen
import com.exory550.exorygallery.presentation.screens.settings.SettingsScreen
import com.exory550.exorygallery.presentation.screens.splash.SplashScreen
import com.exory550.exorygallery.presentation.screens.statistics.StatisticsScreen
import com.exory550.exorygallery.presentation.screens.tools.CleanupScreen
import com.exory550.exorygallery.presentation.screens.tools.ConverterScreen
import com.exory550.exorygallery.presentation.screens.vault.VaultScreen
import com.exory550.exorygallery.presentation.screens.vault.VaultUnlockScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Gallery : Screen("gallery")
    object Albums : Screen("albums")
    object MediaViewer : Screen("media/{mediaId}") {
        fun createRoute(mediaId: Long) = "media/"
    }
    object Map : Screen("map")
    object Search : Screen("search")
    object Vault : Screen("vault")
    object VaultUnlock : Screen("vault_unlock")
    object Cleanup : Screen("cleanup")
    object Converter : Screen("converter")
    object Settings : Screen("settings")
    object Statistics : Screen("statistics")
}

@Composable
fun ExoryNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Gallery.route) { GalleryScreen(navController) }
        composable(Screen.Albums.route) { AlbumsScreen(navController) }
        composable(
            route = Screen.MediaViewer.route,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) { backStackEntry ->
            MediaViewerScreen(navController, backStackEntry.arguments?.getLong("mediaId") ?: 0L)
        }
        composable(Screen.Map.route) { MapScreen(navController) }
        composable(Screen.Search.route) { SearchScreen(navController) }
        composable(Screen.Vault.route) { VaultScreen(navController) }
        composable(Screen.VaultUnlock.route) { VaultUnlockScreen(navController) }
        composable(Screen.Cleanup.route) { CleanupScreen(navController) }
        composable(Screen.Converter.route) { ConverterScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.Statistics.route) { StatisticsScreen(navController) }
    }
}
