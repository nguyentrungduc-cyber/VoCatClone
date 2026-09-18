package com.vocatclone.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vocatclone.app.R

/**
 * Màn hình chính — danh sách các bộ từ vựng (bookcase) của người dùng.
 * Đây là điểm khởi đầu của luồng "Quản lý từ vựng" (tính năng lõi, mục 1-2).
 *
 * Hiện tại chỉ là khung UI cơ bản để chạy thử project — chưa nối với
 * Room database thật (sẽ làm ở ViewModel + Repository khi triển khai đầy đủ).
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // Placeholder state — sẽ thay bằng dữ liệu thật từ ViewModel/Room sau này
    var bookcaseCount by remember { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { bookcaseCount++ }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_bookcase))
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineSmall
            )
            if (bookcaseCount == 0) {
                Text(
                    text = stringResource(R.string.empty_state_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
