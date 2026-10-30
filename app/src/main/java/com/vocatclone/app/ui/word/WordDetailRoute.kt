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
 * và các thao tác trong menu ba chấm (đã có hộp thoại xác nhận trong giao diện).
 *
 * @param onEdit mở form sửa; truyền id bộ từ vựng đang chứa từ.
 * Chuyển sang bộ khác và đổi ngày giờ đã nối. Đánh dấu đã thuộc chưa nối (thuộc task riêng).
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
            currentBookcaseId = state.bookcaseId,
            moveTargets = state.moveTargets,
            onEdit = { onEdit(state.bookcaseId) },
            onMove = viewModel::move,
            onChangeDateTime = viewModel::changeDateTime,
            onDelete = viewModel::delete
        )
    }
}
