package com.exory550.exorygallery.presentation.screens.viewer

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoMenuSheet(
    path: String,
    context: Context,
    onDismiss: () -> Unit,
    onRotate: () -> Unit,
    onProperties: () -> Unit,
    onRename: () -> Unit,
    onHide: () -> Unit,
    onCopyTo: () -> Unit,
    onMoveTo: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onOpenWith: () -> Unit,
    onSetAs: () -> Unit,
    onAddFavorite: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(File(path).name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            HorizontalDivider()
            PhotoMenuItem(Icons.Default.RotateRight, "Rotasi") { onRotate(); onDismiss() }
            PhotoMenuItem(Icons.Default.Info, "Properti") { onProperties(); onDismiss() }
            PhotoMenuItem(Icons.Default.Edit, "Ubah nama") { onRename(); onDismiss() }
            PhotoMenuItem(Icons.Default.VisibilityOff, "Sembunyikan") { onHide(); onDismiss() }
            PhotoMenuItem(Icons.Default.FileCopy, "Salin ke") { onCopyTo(); onDismiss() }
            PhotoMenuItem(Icons.Default.DriveFileMove, "Pindah ke") { onMoveTo(); onDismiss() }
            PhotoMenuItem(Icons.Default.OpenInNew, "Buka dengan") { onOpenWith(); onDismiss() }
            PhotoMenuItem(Icons.Default.Share, "Bagikan") { onShare(); onDismiss() }
            PhotoMenuItem(Icons.Default.Wallpaper, "Atur sebagai") { onSetAs(); onDismiss() }
            PhotoMenuItem(Icons.Default.Tune, "Sunting / Edit") { onEdit(); onDismiss() }
            PhotoMenuItem(Icons.Default.FavoriteBorder, "Tambahkan ke favorit") { onAddFavorite(); onDismiss() }
            PhotoMenuItem(Icons.Default.SelectAll, "Pilih semua") { onSelectAll(); onDismiss() }
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Hapus", fontSize = 15.sp, color = MaterialTheme.colorScheme.error) },
                leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { onDelete(); onDismiss() }
            )
        }
    }
}

@Composable
fun PhotoMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label, fontSize = 15.sp) },
        leadingContent = { Icon(icon, null) },
        modifier = Modifier.clickable { onClick() }
    )
}
