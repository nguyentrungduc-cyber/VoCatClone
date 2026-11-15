package com.vocatclone.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Màn tạm cho route đã có nhưng chưa nối dữ liệu (ViewModel/Repository làm ở task sau).
 * Khi nối xong thì thay bằng màn thật trong [AppNavHost].
 */
@Composable
fun PlaceholderScreen(title: String, detail: String, onBack: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onBack) { Text("Quay lại") }
        }
    }
}
