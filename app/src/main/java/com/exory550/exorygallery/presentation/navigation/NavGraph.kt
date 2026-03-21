package com.exory550.exorygallery.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.exory550.exorygallery.presentation.screens.albums.AlbumsScreen
import com.exory550.exorygallery.presentation.screens.editor.ImageEditorScreen
import com.exory550.exorygallery.presentation.screens.folder.FolderContentScreen
import com.exory550.exorygallery.presentation.screens.gallery.GalleryScreen
import com.exory550.exorygallery.presentation.screens.main.MainScreen
import com.exory550.exorygallery.presentation.screens.map.MapScreen
import com.exory550.exorygallery.presentation.screens.media.MediaViewerScreen
import com.exory550.exorygallery.presentation.screens.picker.FolderPickerScreen
import com.exory550.exorygallery.presentation.screens.search.SearchScreen
import com.exory550.exorygallery.presentation.screens.settings.SettingsScreen
import com.exory550.exorygallery.presentation.screens.splash.SplashScreen
import com.exory550.exorygallery.presentation.screens.statistics.StatisticsScreen
import com.exory550.exorygallery.presentation.screens.tools.CleanupScreen
import com.exory550.exorygallery.presentation.screens.tools.ConverterScreen
import com.exory550.exorygallery.presentation.screens.vault.VaultScreen
import com.exory550.exorygallery.presentation.screens.vault.VaultUnlockScreen
import com.exory550.exorygallery.presentation.screens.video.VideoPlayerScreen
import com.exory550.exorygallery.presentation.screens.viewer.ConflictAction
import com.exory550.exorygallery.presentation.screens.viewer.PhotoActionsHelper
import com.exory550.exorygallery.presentation.screens.viewer.PhotoViewerScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object Home : Screen("home")
    object Gallery : Screen("gallery")
    object Albums : Screen("albums")
    object MediaViewer : Screen("media/{mediaId}") {
        fun createRoute(mediaId: Long) = "media/$mediaId"
    }
    object FolderContent : Screen("folder/{folderName}/{folderPath}") {
        fun createRoute(folderName: String, folderPath: String) =
            "folder/${URLEncoder.encode(folderName, "UTF-8")}/${URLEncoder.encode(folderPath, "UTF-8")}"
    }
    object PhotoViewer : Screen("photo/{photoPath}") {
        fun createRoute(photoPath: String) = "photo/${URLEncoder.encode(photoPath, "UTF-8")}"
    }
    object PhotoPager : Screen("photo_pager/{photoPath}?all={all}") {
        fun createRoute(photoPath: String, allEncoded: String = "") =
            "photo_pager/${URLEncoder.encode(photoPath, "UTF-8")}?all=$allEncoded"
    }
    object VideoPlayer : Screen("video/{videoPath}") {
        fun createRoute(videoPath: String) = "video/${URLEncoder.encode(videoPath, "UTF-8")}"
    }
    object ImageEditor : Screen("editor/{imagePath}") {
        fun createRoute(path: String) = "editor/${URLEncoder.encode(path, "UTF-8")}"
    }
    object FolderPicker : Screen("folder_picker/{action}/{filePath}") {
        fun createRoute(action: String, encodedFilePath: String) = "folder_picker/$action/$encodedFilePath"
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExoryNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        enterTransition = { fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(220)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.96f, animationSpec = tween(220)) }
    ) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Main.route) { MainScreen(navController) }
        composable(Screen.Gallery.route) { GalleryScreen(navController) }
        composable(Screen.Albums.route) { AlbumsScreen(navController) }
        composable(
            route = Screen.MediaViewer.route,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) { back -> MediaViewerScreen(navController, back.arguments?.getLong("mediaId") ?: 0L) }
        composable(
            route = Screen.FolderContent.route,
            arguments = listOf(
                navArgument("folderName") { type = NavType.StringType },
                navArgument("folderPath") { type = NavType.StringType }
            )
        ) { back ->
            FolderContentScreen(
                navController,
                URLDecoder.decode(back.arguments?.getString("folderName") ?: "", "UTF-8"),
                URLDecoder.decode(back.arguments?.getString("folderPath") ?: "", "UTF-8")
            )
        }
        composable(
            route = Screen.PhotoViewer.route,
            arguments = listOf(navArgument("photoPath") { type = NavType.StringType })
        ) { back ->
            PhotoViewerScreen(navController, back.arguments?.getString("photoPath") ?: "")
        }
        composable(
            route = Screen.PhotoPager.route,
            arguments = listOf(
                navArgument("photoPath") { type = NavType.StringType },
                navArgument("all") { type = NavType.StringType; defaultValue = "" }
            )
        ) { back ->
            val encodedPath = back.arguments?.getString("photoPath") ?: ""
            val allEncoded = back.arguments?.getString("all") ?: ""
            val allPhotos = if (allEncoded.isNotBlank())
                allEncoded.split(",").map { URLDecoder.decode(it, "UTF-8") }
            else emptyList()
            PhotoViewerScreen(navController, encodedPath, allPhotos)
        }
        composable(
            route = Screen.VideoPlayer.route,
            arguments = listOf(navArgument("videoPath") { type = NavType.StringType })
        ) { back ->
            VideoPlayerScreen(navController, back.arguments?.getString("videoPath") ?: "")
        }
        composable(
            route = Screen.ImageEditor.route,
            arguments = listOf(navArgument("imagePath") { type = NavType.StringType })
        ) { back ->
            ImageEditorScreen(navController, back.arguments?.getString("imagePath") ?: "")
        }
        composable(
            route = Screen.FolderPicker.route,
            arguments = listOf(
                navArgument("action") { type = NavType.StringType },
                navArgument("filePath") { type = NavType.StringType }
            )
        ) { back ->
            val action = back.arguments?.getString("action") ?: "copy"
            val encodedFilePath = back.arguments?.getString("filePath") ?: ""
            val filePath = URLDecoder.decode(encodedFilePath, "UTF-8")
            val title = if (action == "move") "Pindah ke" else "Salin ke"

            FolderPickerScreen(
                navController = navController,
                title = title,
                onFolderSelected = { destFolder ->
                    CoroutineScope(Dispatchers.Main).launch {
                        val result = if (action == "move") {
                            PhotoActionsHelper.moveFile(filePath, destFolder)
                        } else {
                            PhotoActionsHelper.copyFile(filePath, destFolder)
                        }
                        if (result.conflict) {
                            navController.previousBackStackEntry?.savedStateHandle?.set("conflict_action", action)
                            navController.previousBackStackEntry?.savedStateHandle?.set("conflict_dest", destFolder)
                            navController.previousBackStackEntry?.savedStateHandle?.set("conflict_filename", result.conflictFileName)
                            navController.previousBackStackEntry?.savedStateHandle?.set("show_conflict", true)
                        }
                        navController.popBackStack()
                    }
                }
            )
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
