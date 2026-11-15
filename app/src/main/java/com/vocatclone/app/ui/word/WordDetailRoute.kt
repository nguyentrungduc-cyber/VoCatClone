package com.vocatclone.app.ui.word

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Nối [WordDetailViewModel] vào [WordDetailScreen]: xem đầy đủ một từ (tự cập nhật khi từ đổi) và xóa từ
 * (menu ba chấm, đã có hộp thoại xác nhận trong giao diện).
 *
 * @param onEdit mở form sửa; truyền id bộ từ vựng đang chứa từ.
 * Chuyển bộ, đổi ngày giờ và đánh dấu đã thuộc được nối ở các PR sau.
 */
@Composable
fun WordDetailRoute(
    wordId: Long,
    onBack: () -> Unit,
    onEdit: (bookcaseId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WordDetailViewModel = viewModel()
) {
    LaunchedEffect(wordId) { viewModel.start(wordId) }
    val state by viewModel.state.collectAsState()

    // Từ đã bị xóa (ở đây hoặc ở màn khác): đóng màn.
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }

    val word = state.word
    if (word == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        WordDetailScreen(
            bookcaseName = state.bookcaseName,
            word = word,
            modifier = modifier,
            onBack = onBack,
            onEdit = { onEdit(state.bookcaseId) },
            onDelete = viewModel::delete
        )
    }
}
