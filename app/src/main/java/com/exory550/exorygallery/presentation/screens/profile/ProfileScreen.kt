package com.exory550.exorygallery.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.exory550.exorygallery.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("exory550", style = MaterialTheme.typography.titleLarge)
                        Text("ExoryGallery", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            ProfileMenuItem(Icons.Default.Settings, "Pengaturan") { navController.navigate(Screen.Settings.route) }
            ProfileMenuItem(Icons.Default.PieChart, "Statistik") { navController.navigate(Screen.Statistics.route) }
            ProfileMenuItem(Icons.Default.Lock, "Vault") { navController.navigate(Screen.Vault.route) }
            ProfileMenuItem(Icons.Default.DeleteSweep, "Pembersih") { navController.navigate(Screen.Cleanup.route) }
            ProfileMenuItem(Icons.Default.SwapHoriz, "Konverter") { navController.navigate(Screen.Converter.route) }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
        }
    }
}
