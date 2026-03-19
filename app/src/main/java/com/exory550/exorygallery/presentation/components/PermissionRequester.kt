package com.exory550.exorygallery.presentation.components

import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequester(
    permissions: List<String>,
    onGranted: @Composable () -> Unit,
    onDenied: @Composable () -> Unit
) {
    val state = rememberMultiplePermissionsState(permissions)
    LaunchedEffect(Unit) { state.launchMultiplePermissionRequest() }
    if (state.allPermissionsGranted) onGranted() else onDenied()
}
