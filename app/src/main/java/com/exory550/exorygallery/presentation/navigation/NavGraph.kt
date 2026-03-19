package com.exory550.exorygallery.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.exory550.exorygallery.presentation.screens.albums.AlbumsScreen
import com.exory550.exorygallery.presentation.screens.folder.FolderContentScreen
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
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Gallery : Screen("gallery")
    object Albums : Screen("albums")
    object MediaViewer : Screen("media/{mediaId}") {
        fun createRoute(mediaId: Long) = "media/$mediaId"
    }
    object FolderContent : Screen("folder/{folderName}/{folderPath}") {
        fun createRoute(folderName: String, folderPath: String): String {
            val encodedPath = URLEncoder.encode(folderPath, "UTF-8")
            val encodedName = URLEncoder.encode(folderName, "UTF-8")
            return "folder/$encodedName/$encodedPath"
        }
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
        ) { back ->
            MediaViewerScreen(navController, back.arguments?.getLong("mediaId") ?: 0L)
        }
        composable(
            route = Screen.FolderContent.route,
            arguments = listOf(
                navArgument("folderName") { type = NavType.StringType },
                navArgument("folderPath") { type = NavType.StringType }
            )
        ) { back ->
            val name = URLDecoder.decode(back.arguments?.getString("folderName") ?: "", "UTF-8")
            val path = URLDecoder.decode(back.arguments?.getString("folderPath") ?: "", "UTF-8")
            FolderContentScreen(navController, name, path)
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
