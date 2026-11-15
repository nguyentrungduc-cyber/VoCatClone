package com.vocatclone.app.ui.home

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Nối [HomeViewModel] vào [HomeScreen]: danh sách tủ sách và bộ từ vựng của tài khoản đang đăng nhập,
 * tự cập nhật khi database đổi. [HomeScreen] vẫn chỉ vẽ giao diện (xem được bằng Preview).
 *
 * @param modifier chứa innerPadding của Scaffold ngoài (để không bị thanh điều hướng dưới che).
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onAddBookcase: () -> Unit = {},
    onOpenBookcase: (bookcaseId: Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val shelves by viewModel.shelves.collectAsState()

    // Nút seed chỉ hiện ở bản debug và khi chưa có dữ liệu.
    val context = LocalContext.current
    val isDebuggable = remember { (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 }

    Box(modifier = modifier) {
        HomeScreen(
            modifier = Modifier.fillMaxSize(),
            shelves = shelves,
            onAddClick = onAddBookcase,
            onBookcaseClick = onOpenBookcase
        )
        if (isDebuggable && shelves.isEmpty()) {
            OutlinedButton(
                onClick = viewModel::seedSampleData,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 24.dp)
            ) {
                Text("Thêm dữ liệu mẫu (debug)")
            }
        }
    }
}
