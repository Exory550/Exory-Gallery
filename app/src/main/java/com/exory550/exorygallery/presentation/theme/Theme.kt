package com.exory550.exorygallery.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ExoryHighlight,
    secondary = ExoryAccent,
    tertiary = ExorySecondary,
    background = ExoryBackground,
    surface = ExorySurface,
    onPrimary = ExoryOnPrimary,
    onBackground = ExoryOnBackground,
    onSurface = ExoryOnBackground,
    error = ExoryError
)

private val LightColorScheme = lightColorScheme(
    primary = ExoryAccent,
    secondary = ExorySecondary,
    tertiary = ExoryPrimary,
    background = Color(0xFFF4F4F8),
    surface = Color(0xFFFFFFFF),
    onPrimary = ExoryOnPrimary,
    onBackground = ExoryPrimary,
    onSurface = ExoryPrimary,
    error = ExoryError
)

@Composable
fun ExoryGalleryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExoryTypography,
        content = content
    )
}
