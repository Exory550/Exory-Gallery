package com.exory550.exorygallery.presentation.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.navigation.Screen

@Composable
fun VaultUnlockScreen(navController: NavController, viewModel: VaultViewModel = hiltViewModel()) {
    var pin by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Masukkan PIN Vault", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6) pin = it },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Button(onClick = {
                navController.navigate(Screen.Vault.route) {
                    popUpTo(Screen.VaultUnlock.route) { inclusive = true }
                }
            }) { Text("Buka") }
        }
    }
}
